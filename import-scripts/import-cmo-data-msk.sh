#!/bin/bash

# set necessary env variables with automation-environment.sh

tmp=$PORTAL_HOME/tmp/import-cron-cmo-msk
if [[ -d "$tmp" && "$tmp" != "/" ]] ; then
	rm -rf "$tmp"/*
fi

now=$(date "+%Y-%m-%d-%H-%M-%S")
msk_automation_notification_file=$(mktemp $tmp/msk-automation-portal-update-notification.$now.XXXXXX)

# import vetted studies into MSK portal
echo "importing cancer type updates into msk portal database..."
$JAVA_HOME/bin/java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=27184 -Xmx16g -ea -Dspring.profiles.active=dbcp -Djava.io.tmpdir="$tmp" -cp $PORTAL_HOME/lib/msk-cmo-importer.jar org.mskcc.cbio.importer.Admin -import_types_of_cancer
echo "importing study data into msk portal database..."
$JAVA_HOME/bin/java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=27184 -Xmx64g -ea -Dspring.profiles.active=dbcp -Djava.io.tmpdir="$tmp" -cp $PORTAL_HOME/lib/msk-cmo-importer.jar org.mskcc.cbio.importer.Admin -update_study_data msk-automation-portal:t:$msk_automation_notification_file:t
num_studies_updated=$?

# redeploy war
if [ $num_studies_updated -gt 0 ]
then
	echo "'$num_studies_updated' studies have been updated, requesting redeployment of msk portal war..."
	ssh -i $HOME/.ssh/id_rsa_tomcat_restarts_key cbioportal_importer@dashi.cbio.mskcc.org touch /srv/data/portal-cron/schultz-tomcat-restart
	#echo "'$num_studies_updated' studies have been updated (no longer need to restart schultz-tomcat server...)"
else
	#echo "No studies have been updated, skipping redeploy of msk portal war..."
	echo "No studies have been updated.."
fi

$JAVA_HOME/bin/java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=27184 -Xmx16g -ea -Dspring.profiles.active=dbcp -Djava.io.tmpdir="$tmp" -cp $PORTAL_HOME/lib/msk-cmo-importer.jar org.mskcc.cbio.importer.Admin -send_update_notification msk-automation-portal:$msk_automation_notification_file

if [[ -d "$tmp" && "$tmp" != "/" ]] ; then
	rm -rf "$tmp"/*
fi