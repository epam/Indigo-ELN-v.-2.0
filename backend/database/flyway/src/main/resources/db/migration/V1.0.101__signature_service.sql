DROP TABLE Signature_Template_Block;
DROP TABLE Signature_Template;

ALTER TABLE Experiment DROP COLUMN report_for_signature_id;
ALTER TABLE Experiment ADD COLUMN signature_number VARCHAR(1024);
DROP TABLE Experiment_Signature;
