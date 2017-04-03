#!/bin/bash


MAVEN_BIN=`which mvn 2>/dev/null`

if [[ "$MAVEN_BIN" == "" ]]; then
  MAVEN_BIN="/data/2/user/ddn/work/tools/maven/bin/mvn"
fi

$MAVEN_BIN -Dmaven.test.failure.ignore=true clean package assembly:attached site

## -- Print out summary
SITEPATH=target/site
which html2text >/dev/null 2>&1
if [[ $? != 0 ]]; then
   echo "  ERROR: Can not find html2text for summary processing..."
   echo "         Please install from epel repository..."
   exit 1
fi

write_summary() {
  echo "==========  Summary:  ================"
  echo "date:   `date`  "
  echo "branch: `git branch | grep \* | gawk '{ print $2 }'` : commit : `git log -1 | head -1 | gawk '{ print $2}'`"
  echo "======================================"
  echo "**FindBugs**"
  html2text $SITEPATH/findbugs.html | grep -A 2 Summary

  echo "**Jacoco**"
  html2text $SITEPATH/jacoco/index.html | grep -B2 Total
  echo "**SureFire**"
  html2text $SITEPATH/surefire-report.html  | grep -A 5 "^Summary" | grep -A 2 Tests
  echo "**CheckStyle**"
  html2text $SITEPATH/checkstyle.html | grep -A 2 ^Summary
}

write_summary | sed 's/^/ /'
