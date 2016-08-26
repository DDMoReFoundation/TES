--------------------------------------------------------
--  DDL for Table JOBS
--------------------------------------------------------

  CREATE TABLE JOBS 
   (	"JOB_ID" VARCHAR2(255 CHAR), 
	"CLIENT_REQUEST_STATUS" VARCHAR2(255 CHAR), 
	"CONNECTOR_ID" VARCHAR2(255 CHAR), 
	"DETAILED_STATUS" CLOB, 
	"EXECUTION_REQUEST_MESSAGE" CLOB, 
	"PASSWORD" VARCHAR2(255 CHAR), 
	"REQUEST_DIRECTORY_NAME" VARCHAR2(255 CHAR), 
	"TIMESTAMP_FILE_PATH" VARCHAR2(255 CHAR), 
	"USER_NAME" VARCHAR2(255 CHAR), 
	"VERSION" NUMBER(19,0)
   ) ;
--------------------------------------------------------
--  DDL for Table JOBS_DATA
--------------------------------------------------------

  CREATE TABLE JOBS_DATA 
   (	"JOB_ID" VARCHAR2(255 CHAR), 
	"JOB_DATA_VALUE" VARCHAR2(255 CHAR), 
	"JOB_DATA_KEY" VARCHAR2(255 CHAR)
   ) ;
--------------------------------------------------------
--  Constraints for Table JOBS
--------------------------------------------------------

  ALTER TABLE JOBS MODIFY ("JOB_ID" NOT NULL ENABLE);
 
  ALTER TABLE JOBS ADD PRIMARY KEY ("JOB_ID") ENABLE;
--------------------------------------------------------
--  Constraints for Table JOBS_DATA
--------------------------------------------------------

  ALTER TABLE JOBS_DATA MODIFY ("JOB_ID" NOT NULL ENABLE);
 
  ALTER TABLE JOBS_DATA MODIFY ("JOB_DATA_KEY" NOT NULL ENABLE);
 
  ALTER TABLE JOBS_DATA ADD PRIMARY KEY ("JOB_ID", "JOB_DATA_KEY") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table JOBS_DATA
--------------------------------------------------------

  ALTER TABLE JOBS_DATA ADD CONSTRAINT "FK209D11F3931D4C6E" FOREIGN KEY ("JOB_ID")
	  REFERENCES JOBS ("JOB_ID") ENABLE;
