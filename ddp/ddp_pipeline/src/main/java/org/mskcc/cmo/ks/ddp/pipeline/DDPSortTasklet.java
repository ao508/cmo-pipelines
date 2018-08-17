/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
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
package org.mskcc.cmo.ks.ddp.pipeline;

import org.mskcc.cmo.ks.ddp.pipeline.model.ClinicalRecord;
import org.mskcc.cmo.ks.ddp.pipeline.model.TimelineChemoRecord;
import org.mskcc.cmo.ks.ddp.pipeline.model.TimelineRadiationRecord;
import org.mskcc.cmo.ks.ddp.pipeline.model.TimelineSurgeryRecord;
import org.mskcc.cmo.ks.ddp.pipeline.util.DDPUtils;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;

import org.apache.commons.lang.StringUtils;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 * @author Avery Wang
 */
public class DDPSortTasklet implements Tasklet {

    Logger log = Logger.getLogger(DDPSortTasklet.class);

    @Value("#{jobParameters[outputDirectory]}")
    private String outputDirectory;

    private final String clinicalFilename = "data_clinical_ddp.txt";
    private final String timelineChemotherapyFilename = "data_timeline_ddp_chemotherapy.txt";
    private final String timelineRadiationFilename = "data_timeline_ddp_radiation.txt";
    private final String timelineSurgeryFilename = "data_timeline_ddp_surgery.txt";

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        String clinicalFilePath = Paths.get(outputDirectory, clinicalFilename).toString();
        String clinicalHeader = StringUtils.join(ClinicalRecord.getFieldNames(), "\t");
        log.info("Sorting and overwriting " + clinicalFilePath);
        DDPUtils.sortAndWrite(clinicalFilePath, clinicalHeader);

        String timelineChemotherapyFilePath = Paths.get(outputDirectory, timelineChemotherapyFilename).toString();
        String timelineChemotherapyHeader = StringUtils.join(TimelineChemoRecord.getFieldNames(), "\t");
        log.info("Sorting and overwriting " + timelineChemotherapyFilePath);
        DDPUtils.sortAndWrite(timelineChemotherapyFilePath, timelineChemotherapyHeader);

        String timelineRadiationFilePath = Paths.get(outputDirectory, timelineRadiationFilename).toString();
        String timelineRadiationHeader = StringUtils.join(TimelineRadiationRecord.getFieldNames(), "\t");
        log.info("Sorting and overwriting " + timelineRadiationFilePath);
        DDPUtils.sortAndWrite(timelineRadiationFilePath, timelineRadiationHeader);

        String timelineSurgeryFilePath = Paths.get(outputDirectory, timelineSurgeryFilename).toString();
        String timelineSurgeryHeader = StringUtils.join(TimelineSurgeryRecord.getFieldNames(), "\t");
        log.info("Sorting and overwriting " + timelineSurgeryFilePath);
        DDPUtils.sortAndWrite(timelineSurgeryFilePath, timelineSurgeryHeader);

        return RepeatStatus.FINISHED;
    }
}
