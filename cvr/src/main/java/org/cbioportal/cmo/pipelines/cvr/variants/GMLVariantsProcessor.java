/*
 * Copyright (c) 2016 - 2017 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal CMO-Pipelines.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cbioportal.cmo.pipelines.cvr.variants;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.apache.log4j.Logger;
import org.cbioportal.cmo.pipelines.cvr.CVRUtilities;
import org.cbioportal.cmo.pipelines.cvr.model.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author jake
 */
public class GMLVariantsProcessor implements ItemProcessor<GMLVariant, String> {

    @Value("${dmp.server_name}")
    private String dmpServerName;

    @Value("${dmp.tokens.consume_gml_sample}")
    private String dmpConsumeSample;

    @Value("#{jobParameters[sessionId]}")
    private String sessionId;

    @Value("#{jobParameters[testingMode]}")
    private boolean testingMode;
    
    Logger log = Logger.getLogger(GMLVariantsProcessor.class);

    @Autowired
    public CVRUtilities cvrUtilities;

    private String gmlConsumeUrl;

    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = getRequestEntity();

    @Override
    public String process(GMLVariant g) throws Exception {
        gmlConsumeUrl = dmpServerName + dmpConsumeSample + "/" + sessionId + "/";
        HashMap<String, GMLResult> results = g.getResults();
        GMLData gmlData = new GMLData(g.getSampleCount(), g.getDisclaimer(), new ArrayList<GMLResult>());
        ObjectMapper mapper = new ObjectMapper();
        for (Map.Entry<String, GMLResult> pair : results.entrySet()) {
            GMLResult result = pair.getValue();
            String patientId = result.getMetaData().getDmpPatientId();
            cvrUtilities.addNewId(patientId);
            String sampleId = pair.getKey();
            
            if (!testingMode) {
                consumeSample(sampleId);
            }
            gmlData.addResult(result);
        }
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(gmlData);
    }

    private HttpEntity getRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(headers);
    }

    private void consumeSample(String sampleId) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<CVRConsumeSample> responseEntity;
        try {
            responseEntity = restTemplate.exchange(gmlConsumeUrl + sampleId, HttpMethod.GET, requestEntity, CVRConsumeSample.class);
            if (responseEntity.getBody().getaffectedRows()==0) {
                String message = "No consumption for sample " + sampleId;
                log.warn(message);
            }
            if (responseEntity.getBody().getaffectedRows()>1) {
                String message = "Multple samples consumed (" + responseEntity.getBody().getaffectedRows() + ") for sample " + sampleId;
                log.warn(message);
            }
            if (responseEntity.getBody().getaffectedRows()==1) {
                String message = "Sample "+ sampleId +" consumed succesfully";
                log.info(message);
            }
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("Error consuming sample " + sampleId + "(" + e + ")");
        }
    }
}