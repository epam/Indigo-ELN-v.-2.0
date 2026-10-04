ALTER TABLE Experiment DROP COLUMN signature_attachment_id, DROP COLUMN signature_number;

UPDATE Experiment_Revision SET mutation = mutation - 'attachmentID' WHERE mutation ->> 'type' = 'SignatureUpdated';
