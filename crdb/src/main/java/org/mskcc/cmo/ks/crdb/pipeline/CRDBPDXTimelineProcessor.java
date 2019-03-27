/*
 * Copyright (c) 2016 - 2018 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cmo.ks.crdb.pipeline;

import java.util.*;
import org.mskcc.cmo.ks.crdb.pipeline.model.CRDBPDXTimelineDataset;
import org.mskcc.cmo.ks.crdb.pipeline.util.CRDBUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Class for processing the CRDB Dataset results for the staging file.
 *
 * @author ochoaa
 */

public class CRDBPDXTimelineProcessor implements ItemProcessor<CRDBPDXTimelineDataset, String> {

    @Value("#{stepExecutionContext['crdbPdxFieldOrder']}")
    private List<String> crdbPdxTimelineFieldOrder;

    @Autowired
    private CRDBUtils crdbUtils;

    @Override
    public String process(final CRDBPDXTimelineDataset crdbPDXTimelineDataset) throws Exception {
        return getFormattedRecord(crdbPDXTimelineDataset);
    }

    public String getFormattedRecord(CRDBPDXTimelineDataset crdbPDXTimelineDataset) throws Exception {
        List<String> record = new ArrayList<>();
        for (String field : crdbPdxTimelineFieldOrder) {
            String value = crdbPDXTimelineDataset.getClass().getMethod("get" + field).invoke(crdbPDXTimelineDataset).toString();
            record.add(crdbUtils.convertWhitespace(value));
        }
        return String.join("\t", record);
    }
}
