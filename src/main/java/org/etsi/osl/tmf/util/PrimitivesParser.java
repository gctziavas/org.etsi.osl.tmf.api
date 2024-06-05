/**
 * @Author: Eduardo Santos
 * @Date:   2024-05-30 12:49:06
 * @Last Modified by:   Eduardo Santos
 * @Last Modified time: 2024-05-31 13:27:02
 */

 /*-
 * ========================LICENSE_START=================================
 * org.etsi.osl.tmf.api
 * %%
 * Copyright (C) 2019 openslice.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package org.etsi.osl.tmf.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.etsi.osl.model.nfv.NetworkServiceDescriptor;
import org.etsi.osl.model.nfv.ConstituentVxF;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is responsible for parsing available primitives from a Network Service
* from its Network Service Descriptor (NSD) and Virtual Network Function Descriptor (VNFD).
*/
public class PrimitivesParser {

    private static final transient Log logger = LogFactory.getLog(PrimitivesParser.class.getName());

    public static Map<String, String> cpdToVduMap = new HashMap<String, String>();


    /**
     * Extracts all available primitives from a given Network Service Descriptor (NSD).
    * This method processes the NSD to retrieve and map its constituent VNF descriptors (VNFDs) and Virtual Deployment Units (VDUs),
    * then extracts the configuration primitives associated with each VDU and VNF, returning a consolidated JSON object with all primitives.
    *
    * @param nsd The NetworkServiceDescriptor object containing the network service data.
    * @return A JSONObject containing all the extracted primitives from the NSD.
    */
    public static JSONObject extractPrimitives(NetworkServiceDescriptor nsd) {
        JSONObject allPrimitives = new JSONObject();

        try {
            // Access the first element only, to handle the case where there are duplicate descriptor fields
            ConstituentVxF vxf = nsd.getConstituentVxF().get(0);
            
            if (vxf.getVxfref() == null) {
                throw new NullPointerException("vxf.getVxfref() is null");
            }
            
            JSONObject nsdJson = PrimitivesParser.processNSD(nsd);
            JSONObject vnfdJson = PrimitivesParser.processVxF(vxf);
    
            JSONObject vnfs = PrimitivesParser.extractVNFIds(nsdJson);
            
            vnfs = PrimitivesParser.mapVDUsToVNFs(vnfdJson, vnfs);

            JSONObject vduPrimitives = PrimitivesParser.extractVDUPrimitives(vnfdJson, vnfs);
    
            allPrimitives = PrimitivesParser.extractVNFPrimitives(vnfdJson, vduPrimitives);
        } catch (NullPointerException e) {
            logger.error("Error extracting primitives: " + e.getMessage());
            e.printStackTrace();
            // allPrimitives is already initialized to an empty JSONObject
        } catch (Exception e) {
            logger.error("Error extracting primitives.");
            e.printStackTrace();
        }

        return allPrimitives;
        
    }
    

    /**
     * Converts a NetworkServiceDescriptor's descriptor from YAML string to a JSONObject.
    * This method utilizes the YAML library to parse the descriptor of the NetworkServiceDescriptor,
    * converting it into a Map and then into a JSONObject for easier manipulation in Java.
    *
    * @param nsd The NetworkServiceDescriptor object containing the YAML string in its descriptor.
    * @return A JSONObject representing the parsed YAML descriptor.
    */
    public static JSONObject processNSD(NetworkServiceDescriptor nsd) {
        // Parse the NSD descriptor from string to YAML
        Yaml yaml = new Yaml();
        Map<String, Object> yamlMap = yaml.load(nsd.getDescriptor());

        // Parse the NSD descriptor from YAML to JSONObject
        JSONObject nsdJson = new JSONObject(yamlMap);

        return nsdJson;
    }


    /**
     * Converts a VNF descriptor from JSON string to JSONObject.
    * This method takes the JSON descriptor from a ConstituentVxF object, manipulates the string to ensure proper JSON format,
    * and converts it into a JSONObject.
    *
    * @param vxf The ConstituentVxF object containing the JSON string of the VNF descriptor.
    * @return A JSONObject representing the VNF descriptor.
    */
    public static JSONObject processVxF(ConstituentVxF vxf) {
        // Parse the VNF descriptor from JSON string to JSONObject
        //JSONObject vnfdJson = new JSONObject(vxf.getVxfref().getDescriptor().substring(1, vxf.getVxfref().getDescriptor().length() - 1));

        String descriptor = vxf.getVxfref().getDescriptor();
        JSONObject vnfdJson;

        // Check if the descriptor starts with a JSON array or object syntax
        if (descriptor.trim().startsWith("[")) {
            // It's JSON
            vnfdJson = new JSONObject(descriptor.substring(1, descriptor.length() - 1));
        } else {
            // It's YAML
            Yaml yaml = new Yaml();
            Map<String, Object> yamlMap = yaml.load(descriptor);
            
            // Convert the Map to a JSONObject
            vnfdJson = new JSONObject(yamlMap).getJSONObject("vnfd");
        }

        return vnfdJson;
    }


    /**
     * Extracts VNF identifiers and their constituent CPD IDs from a NSD.
    * This method serves as the orchestrator that calls other methods to handle specific parts of the JSON structure.
    *
    * @param nsdJson The JSONObject containing the NSD data.
    * @return JSONObject containing an array of VNFs with their IDs and associated constituent CPD IDs.
    */
    public static JSONObject extractVNFIds(JSONObject nsdJson) {
        // JSONObject to hold the VNFs and their details
        JSONObject vnfs = new JSONObject();

        //JSONArray to store each VNF
        JSONArray vnfsArray = new JSONArray();
        
        // Attach the empty array to the JSONObject under the key "vnfs"
        vnfs.put("vnfs", vnfsArray);

        JSONArray nsdArray = nsdJson.getJSONObject("nsd").getJSONArray("nsd");

        // Assuming that there might be multiple 'nsd' entries, iterate over them
        processNSDEntries(nsdArray, vnfsArray);

        return vnfs;
    }


    /**
     * Processes each NSD entry.
    *
    * @param nsdArray The array of NSD entries.
    * @param vnfsArray The array to store processed VNF data.
    */
    private static void processNSDEntries(JSONArray nsdArray, JSONArray vnfsArray) {
        for (int nsdIndex = 0; nsdIndex < nsdArray.length(); nsdIndex++) {
            JSONArray nsdDf = nsdArray.getJSONObject(nsdIndex).getJSONArray("df");
            
            processDeploymentFlavors(nsdDf, vnfsArray);
        }
    }


    /**
     * Processes each deployment flavor within an NSD.
    *
    * @param nsdDf The array of deployment flavors.
    * @param vnfsArray The array to store processed VNF data.
    */
    private static void processDeploymentFlavors(JSONArray nsdDf, JSONArray vnfsArray) {
        for (int dfIndex = 0; dfIndex < nsdDf.length(); dfIndex++) {
            JSONArray vnfProfilesArray = nsdDf.getJSONObject(dfIndex).getJSONArray("vnf-profile");
            
            processVNFProfiles(vnfProfilesArray, vnfsArray);
        }
    }


    /**
     * Processes each VNF profile to extract the VNF details.
    *
    * @param vnfProfilesArray The array of VNF profiles.
    * @param vnfsArray The array to store VNF data.
    */
    private static void processVNFProfiles(JSONArray vnfProfilesArray, JSONArray vnfsArray) {
        for (int vnfIndex = 0; vnfIndex < vnfProfilesArray.length(); vnfIndex++) {
            // For each VNF, extract its id
            JSONObject vnfProfile = vnfProfilesArray.getJSONObject(vnfIndex);
            
            extractVNFDetails(vnfProfile, vnfsArray);
        }
    }


    /**
     * Extracts details from each VNF profile and adds them to the VNFS array.
    *
    * @param vnfProfile The VNF profile JSONObject.
    * @param vnfsArray The array to store VNF data.
    */
    private static void extractVNFDetails(JSONObject vnfProfile, JSONArray vnfsArray) {
        String vnfId = vnfProfile.getString("id");

        // Create a new JSONObject for this VNF, storing its ID
        JSONObject vnfObj = new JSONObject();
        
        vnfObj.put("id", vnfId);

        // Extract virtual link connectivity information for each VNF
        JSONArray virtualLinkConnectivityArray = vnfProfile.getJSONArray("virtual-link-connectivity");
        JSONArray constituentCpdIdArray = new JSONArray();

        for (int vlcIndex = 0; vlcIndex < virtualLinkConnectivityArray.length(); vlcIndex++) {
            // Extract and iterate over the constituent cpd ids for each virtual link connectivity
            JSONArray constituentCpdIds = virtualLinkConnectivityArray.getJSONObject(vlcIndex).getJSONArray("constituent-cpd-id");

            for (int cpdIndex = 0; cpdIndex < constituentCpdIds.length(); cpdIndex++) {
                String constituentCpdId = constituentCpdIds.getJSONObject(cpdIndex).getString("constituent-cpd-id");

                constituentCpdIdArray.put(constituentCpdId);
            }
            
            // Associate the array of constituent cpd ids with the corresponding VNF
            vnfObj.put("constituent-cpd-ids", constituentCpdIdArray);
        }
        vnfsArray.put(vnfObj);
    }


    /**
     * Maps VDUs to VNFs based on the provided JSON representations
    *
    * @param vnfdJson JSON object representing the Virtual Network Function Descriptor (VNFD).
    * @param vnfs JSON object representing the Virtual Network Functions (VNFs).
    * @return JSON object with VNFs updated with VDU IDs.
    */
    public static JSONObject mapVDUsToVNFs(JSONObject vnfdJson, JSONObject vnfs) {
        buildCpdToVduMap(vnfdJson);
        
        return updateVNFsWithVDUIds(vnfs);
    }


    /**
     * Builds a mapping of CPDs to VDU IDs based on the VNFD JSON.
    *
    * @param vnfdJson JSON object representing the VNFD.
    */
    private static void buildCpdToVduMap(JSONObject vnfdJson) {
        // Retrieve the array of external connection point descriptors from the VNFD
        JSONArray extCpdArray = vnfdJson.optJSONArray("ext-cpd");
        
        if (extCpdArray == null) return; // Early return if the array doesn't exist

        // Iterate through each connection point descriptor to map them to VDU ids
        for (int i = 0; i < extCpdArray.length(); i++) {
            JSONObject cpdEntry = extCpdArray.getJSONObject(i);
            String id = cpdEntry.optString("id", null); // cpd id
            JSONObject intCpd = cpdEntry.optJSONObject("int-cpd"); // VDU id linked to this cpd

            if (id != null && intCpd != null) {
                String vduId = intCpd.optString("vdu-id", "Unknown");
                
                // Map each CPD id to its corresponding VDU id
                cpdToVduMap.put(id, vduId);
            }
        }
    }


    /**
     * Updates VNF JSON objects with VDU IDs based on the mapping created by {@link #buildCpdToVduMap(JSONObject)}.
    *
    * @param vnfs JSON object representing the Virtual Network Functions (VNFs).
    * @return JSON object with VNFs updated with VDU IDs.
    */
    private static JSONObject updateVNFsWithVDUIds(JSONObject vnfs) {
        // Modify vnfs JSON using the map
        JSONArray vnfsArray = vnfs.optJSONArray("vnfs");
        
        if (vnfsArray == null) return vnfs; // Early return if no VNFs found

        // Iterate through each VNF to replace "constituent-cpd-ids" with "vdu-ids"
        for (int i = 0; i < vnfsArray.length(); i++) {
            JSONObject vnf = vnfsArray.getJSONObject(i);
            
            updateVNFWithVDUIds(vnf);
        }
        return vnfs;
    }


    /**
     * Updates a single VNF JSON object with VDU IDs by replacing "constituent-cpd-ids" with "vdu-ids".
    *
    * @param vnf JSON object representing a single VNF.
    */
    private static void updateVNFWithVDUIds(JSONObject vnf) {
        JSONArray cpdIds = vnf.optJSONArray("constituent-cpd-ids");
        if (cpdIds == null) return; // Early return if no cpd ids

        JSONArray vduIds = new JSONArray();
        
        // Replace each cpd id with the corresponding VDU id
        for (int j = 0; j < cpdIds.length(); j++) {
            String cpdId = cpdIds.optString(j, null);
            
            if (cpdId != null) {
                // Get the VDU ID associated with the CPD ID, or mark as "Unknown" if no mapping exists
                String vduId = cpdToVduMap.getOrDefault(cpdId, "Unknown");
                
                vduIds.put(vduId);
            }
        }

        // Update the VNF JSON object: replace "constituent-cpd-ids" with "vdu-ids"
        vnf.put("vdu-ids", vduIds);
        vnf.remove("constituent-cpd-ids");
    }

    
    /**
     * Extracts VNF primitives based on management connection points and updates the VNFs with the corresponding VDU configurations.
    *
    * @param vnfdJson The JSON object containing the VNFD data.
    * @param vnfs The JSON object containing VNF configurations.
    * @return JSONObject Updated JSON object with VNFs containing corresponding VDU configurations.
    */
    public static JSONObject extractVNFPrimitives(JSONObject vnfdJson, JSONObject vnfs) {
        // Extract the array of VNF configurations from the provided JSON object
        JSONArray vnfsArray = vnfs.getJSONArray("vnfs");

        // Retrieve the management connection point from the VNFD JSON
        String vnfMgmtCp = vnfdJson.getString("mgmt-cp");

        // Iterate through each VNF in the array
        processVNFs(vnfsArray, vnfMgmtCp);

        return vnfs;
    }


    /**
     * Process each VNF to check and update its configuration based on VDU management connection points.
    *
    * @param vnfsArray JSONArray of VNFs.
    * @param vnfMgmtCp The management connection point identifier from VNFD.
    */
    private static void processVNFs(JSONArray vnfsArray, String vnfMgmtCp) {
        for (int i = 0; i < vnfsArray.length(); i++) {
            // Get the VDU configurations for the current VNF
            JSONArray vduConfigs = vnfsArray.getJSONObject(i).getJSONArray("vdu-configs");
            
            // Iterate through each VDU configuration
            processVDUConfigurations(vduConfigs, vnfsArray.getJSONObject(i), vnfMgmtCp);
        }
    }


    /**
     * Iterates through VDU configurations and checks for matches with the VNF's management connection point to update configuration primitives.
    *
    * @param vduConfigs JSONArray of VDU configurations for a VNF.
    * @param vnf JSONObject of the current VNF.
    * @param vnfMgmtCp The management connection point identifier.
    */
    private static void processVDUConfigurations(JSONArray vduConfigs, JSONObject vnf, String vnfMgmtCp) {
        for (int j = 0; j < vduConfigs.length(); j++) {
            checkAndUpdateVNFConfiguration(vduConfigs.getJSONObject(j), vnf, vnfMgmtCp);
        }
    }


    /**
     * Checks if the VDU configuration matches the management connection point and updates the VNF configuration if it does.
    *
    * @param vduConfig JSONObject of the current VDU configuration.
    * @param vnf JSONObject of the current VNF.
    * @param vnfMgmtCp The management connection point identifier.
    */
    private static void checkAndUpdateVNFConfiguration(JSONObject vduConfig, JSONObject vnf, String vnfMgmtCp) {
        String vduConfigId = vduConfig.getString("id");

        // Check each entry in the map for a matching management connection point
        for (String key : cpdToVduMap.keySet()) {

            // If the VDU's external connection point equal the VNF's management connection point
            // this means that the VDU is the VNF's management VDU, i.e., when invoking the 
            // VNF-level primitives, the invoked primitives will be the magagement VDU ones
            if (key.equals(vnfMgmtCp) && cpdToVduMap.get(key).equals(vduConfigId)) {
                // Update the VNF object with the configuration primitives from the correspondent management VDU 
                vnf.put("config-primitive", vduConfig.getJSONArray("config-primitive"));
            }
        }
    }


    /**
     * Processes VNFD JSON to extract and map VDU primitives based on configurations, then updates VNFs JSON structure accordingly.
    *
    * @param vnfdJson The VNFD JSON object containing deployment flavors and operational configurations.
    * @param vnfs The JSON object containing VNFs that will be updated with VDU configurations.
    * @return JSONObject Updated VNFs JSON object with VDU configurations.
    */
    public static JSONObject extractVDUPrimitives(JSONObject vnfdJson, JSONObject vnfs) {
        JSONArray vnfsArray = vnfs.getJSONArray("vnfs");
        JSONArray dfs = vnfdJson.getJSONArray("df");
        
        for (int i = 0; i < dfs.length(); i++) {
            JSONObject vduToConfig = mapVDUConfigs(dfs.getJSONObject(i));
            updateVNFsWithVDUConfigs(vnfsArray, vduToConfig);
        }
        
        return vnfs;
    }


    /**
     * Maps each VDU ID to its config-primitives, excluding the 'execution-environment-ref' from the primitives.
    *
    * @param df The deployment flavor JSON object.
    * @return JSONObject Mapping of VDU IDs to their config primitives.
    */
    private static JSONObject mapVDUConfigs(JSONObject df) {
        // Mapping from VDU id to config-primitives
        JSONArray day1_2 = df.getJSONObject("lcm-operations-configuration")
                            .getJSONObject("operate-vnf-op-config")
                            .getJSONArray("day1-2");

        if (day1_2 == null || day1_2.length() == 0) {
            logger.error("Error: Day 1-2 configuration array is missing or empty.");

            // Return an empty JSONObject if no data is present
            return new JSONObject();  
        }
                            
        JSONObject vduToConfig = new JSONObject();
        
        for (int i = 0; i < day1_2.length(); i++) {
            JSONObject vduConfig = day1_2.getJSONObject(i);
            
            vduToConfig.put(vduConfig.getString("id"), filterConfigPrimitives(vduConfig.getJSONArray("config-primitive")));
        }
        
        return vduToConfig;
    }


    /**
     * Removes 'execution-environment-ref' from each config primitive and returns a filtered array of config primitives.
    *
    * @param configPrimitives The JSON array of config primitives.
    * @return JSONArray The filtered array of config primitives.
    */
    private static JSONArray filterConfigPrimitives(JSONArray configPrimitives) {
        JSONArray filteredConfigPrimitives = new JSONArray();
        
        // Remove execution-environment-ref from each config-primitive
        for (int j = 0; j < configPrimitives.length(); j++) {
            JSONObject primitive = configPrimitives.getJSONObject(j);
            
            primitive.remove("execution-environment-ref");
            filteredConfigPrimitives.put(primitive);
        }
        
        return filteredConfigPrimitives;
    }


    /**
     * Updates each VNF in the vnfsArray with the mapped VDU configurations.
    *
    * @param vnfsArray The JSON array of VNFs to update.
    * @param vduToConfig The mapping of VDU IDs to their filtered config primitives.
    */
    private static void updateVNFsWithVDUConfigs(JSONArray vnfsArray, JSONObject vduToConfig) {
        // Append the filtered config-primitive to each VDU in the VNFs JSON and remove vdu-ids
        JSONObject vnf;
        for (int i = 0; i < vnfsArray.length(); i++) {
            vnf = vnfsArray.getJSONObject(i);
            
            updateVNFWithVDUConfig(vnf, vduToConfig);
        }
    }


    /**
     * Updates a single VNF with VDU configurations using provided mappings.
    *
    * @param vnf The JSON object of the VNF to update.
    * @param vduToConfig The mapping of VDU IDs to their filtered config primitives.
    */
    private static void updateVNFWithVDUConfig(JSONObject vnf, JSONObject vduToConfig) {
        JSONArray vduIds = vnf.getJSONArray("vdu-ids");
        JSONArray vduConfigs = new JSONArray();
        
        JSONObject vduConfig;
        for (int j = 0; j < vduIds.length(); j++) {
            String vduId = vduIds.getString(j);
            vduConfig = new JSONObject();
            
            vduConfig.put("id", vduId);
            vduConfig.put("config-primitive", vduToConfig.getJSONArray(vduId));
            vduConfigs.put(vduConfig);
        }
        
        vnf.put("vdu-configs", vduConfigs);
        // Removing the 'vdu-ids' key after updating
        vnf.remove("vdu-ids");  
    }
}
 
