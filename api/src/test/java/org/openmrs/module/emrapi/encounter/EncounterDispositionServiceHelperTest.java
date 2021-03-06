package org.openmrs.module.emrapi.encounter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterDispositionServiceHelperTest {


    public static final String TEXT_CONCEPT_UUID = "text-concept-uuid";
    public static final String NUMERIC_CONCEPT_UUID = "numeric-concept-uuid";
    @Mock
    private ConceptService conceptService;
    @Mock
    private ObsService obsService;

    private EncounterDispositionServiceHelper encounterDispositionServiceHelper;
    private static final String UUID_SUFFIX ="-uuid-1234";

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        encounterDispositionServiceHelper = new EncounterDispositionServiceHelper(conceptService);

    }

    @Test
    public void shouldSaveDispositionAsObsGroup() {
   //     String questionConceptUuid = "disposition-question"+UUID_SUFFIX;
        String noteConceptUuid = "disposition-note"+UUID_SUFFIX;
        String dispositionNoteValue = "disposition note text";
        String code = "ADMIT";

    //    newConcept(ConceptDatatype.CODED, questionConceptUuid);
        newConcept(ConceptDatatype.TEXT, noteConceptUuid);

        newConceptByMapping(EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT_SET,null);
        newConceptByMapping(EmrApiConstants.CONCEPT_CODE_DISPOSITION,dispositionAnswers());
        newConceptByMapping(code,null);

        Encounter encounter = new Encounter();
        encounter.setUuid("e-uuid");

        EncounterTransaction.Disposition disposition = new EncounterTransaction.Disposition();
        disposition.setCode(code).setAdditionalObs(Arrays.asList(new EncounterTransaction.Observation().setConcept(getConcept(noteConceptUuid)).setValue(dispositionNoteValue)));

        encounterDispositionServiceHelper.update(encounter, disposition, new Date());

        assertDispositionValues(noteConceptUuid, dispositionNoteValue, code, encounter);


    }

    private EncounterTransaction.Concept getConcept(String conceptUuid) {
        return new EncounterTransaction.Concept(conceptUuid, "concept_name");
    }

    private void assertDispositionValues(String noteConceptUuid, String dispositionNoteValue, String code, Encounter encounter) {
        Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
        assertEquals(1, obsAtTopLevel.size());
        Obs obs = obsAtTopLevel.iterator().next();
        assertTrue(obs.isObsGrouping());
        Set<Obs> obsGroupMembers = obs.getGroupMembers();
        assertEquals(2, obsGroupMembers.size());

        boolean dispositionConceptExists = false;
        boolean noteConceptExists = false;

        for (Obs obsGroupMember : obsGroupMembers) {
            if(obsGroupMember.getConcept().getUuid().equals(EmrApiConstants.CONCEPT_CODE_DISPOSITION+UUID_SUFFIX)){
                dispositionConceptExists =true;
                assertEquals("Disposition answer not being added correctly",code+UUID_SUFFIX,obsGroupMember.getValueCoded().getUuid());
            }
            else if(obsGroupMember.getConcept().getUuid().equals(noteConceptUuid)){
                noteConceptExists =true;
                assertEquals("Error in disposition note value",dispositionNoteValue,obsGroupMember.getValueText());
            }

        }
        assertTrue("disposition not being added correctly",dispositionConceptExists);
        assertTrue("Disposition note is not being added correctly",noteConceptExists);
    }


    @Test
    public void shouldUpdateDispositionAsObsGroup() {
        //     String questionConceptUuid = "disposition-question"+UUID_SUFFIX;
        String noteConceptUuid = "disposition-note"+UUID_SUFFIX;
        String dispositionNoteValue = "disposition note text";
        String code = "ADMIT";

        //    newConcept(ConceptDatatype.CODED, questionConceptUuid);
        newConcept(ConceptDatatype.TEXT, noteConceptUuid);

        newConceptByMapping(EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT_SET,null);
        newConceptByMapping(EmrApiConstants.CONCEPT_CODE_DISPOSITION,dispositionAnswers());
        newConceptByMapping(code,null);

        Encounter encounter = new Encounter();
        encounter.setUuid("e-uuid");

        EncounterTransaction.Disposition disposition = new EncounterTransaction.Disposition();
        disposition.setCode(code).setAdditionalObs(Arrays.asList(new EncounterTransaction.Observation().setConcept(getConcept(noteConceptUuid)).setValue(dispositionNoteValue)));

        encounterDispositionServiceHelper.update(encounter, disposition, new Date());

        code = "DISCHARGE";
        dispositionNoteValue = dispositionNoteValue+" addendum";
        disposition = new EncounterTransaction.Disposition();
        disposition.setCode(code).setAdditionalObs(Arrays.asList(new EncounterTransaction.Observation().setConcept(getConcept(noteConceptUuid)).setValue(dispositionNoteValue)));
        newConceptByMapping(code,null);

        encounterDispositionServiceHelper.update(encounter, disposition, new Date());

        assertDispositionValues(noteConceptUuid, dispositionNoteValue, code, encounter);

    }



//    @Test
//    public void shouldAddNewObservationWithDisposition() throws ParseException {
//        String dipositionAction = EmrApiConstants.DISPOSITION_ANSWER_DISCHARGE;
//
//        newConcept(ConceptDatatype.TEXT, TEXT_CONCEPT_UUID);
//        newConcept(ConceptDatatype.CODED, questionConceptUuid);
//        newConcept(ConceptDatatype.TEXT, dispostionNoteConceptUuid);
//        newConcept(ConceptDatatype.TEXT, dipositionAction+UUID_SUFFIX);
//
//        newConceptByName( EmrApiConstants.DISPOSITION_CONCEPT);
//        newConceptByName( EmrApiConstants.DISPOSITION_ANSWER_ADMIT);
//        newConceptByName( EmrApiConstants.DISPOSITION_ANSWER_DISCHARGE);
//        newConceptByName( EmrApiConstants.DISPOSITION_ANSWER_TRANSFER);
//        newConceptByName( EmrApiConstants.DISPOSITION_ANSWER_REFER);
//        newConceptByName( EmrApiConstants.DISPOSITION_NOTE_CONCEPT);
//
//
//
//        List<EncounterTransaction.Observation> observations = asList(
//                new EncounterTransaction.Observation().setConceptUuid(TEXT_CONCEPT_UUID).setValue("text value").setComment("overweight")
//        );
//
//        EncounterTransaction.Disposition disposition = new EncounterTransaction.Disposition().setCode(dipositionAction);
//        disposition.setDispositionNote(dispostionNoteValue);
//
//        Patient patient = new Patient();
//        Encounter encounter = new Encounter();
//        encounter.setUuid("e-uuid");
//        encounter.setPatient(patient);
//
//        Date observationDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse("2005-01-01T00:00:00.000+0000");
//
//
//        encounterDispositionServiceHelper.update(encounter, disposition, observationDateTime);
//
//        assertEquals(2, encounter.getObs().size());
//
//        Obs adtObservation = getObsByUuid(questionConceptUuid,encounter.getAllObs());
//        assertEquals(patient, adtObservation.getPerson());
//        assertEquals("e-uuid", adtObservation.getEncounter().getUuid());
//        assertEquals(observationDateTime, adtObservation.getObsDatetime());
//
//        assertEquals(dipositionAction+UUID_SUFFIX, adtObservation.getValueCoded().getUuid());
//        assertEquals(questionConceptUuid, adtObservation.getConcept().getUuid());
//
//        adtObservation = getObsByUuid(dispostionNoteConceptUuid,encounter.getAllObs());
//        assertEquals(dispostionNoteValue, adtObservation.getValueText());
//        assertEquals(dispostionNoteConceptUuid, adtObservation.getConcept().getUuid());
//
//    }



    private Obs getObsByUuid(String uuid,Set<Obs> obsSet){
        for(Iterator<Obs> obs = obsSet.iterator();obs.hasNext();){
            Obs observation = obs.next();
            if(observation.getConcept().getUuid().equals(uuid)) {
                return observation;
            }
        }
        return null;
    }

    private Concept newConcept(String hl7, String uuid) {
        Concept concept = new Concept();
        ConceptDatatype textDataType = new ConceptDatatype();
        textDataType.setHl7Abbreviation(hl7);
        concept.setDatatype(textDataType);
        concept.setUuid(uuid);
        when(conceptService.getConceptByUuid(uuid)).thenReturn(concept);
        return concept;
    }

    private Concept newConceptByMapping( String mapping,List<ConceptAnswer> answers) {
        Concept concept = new Concept();
        ConceptDatatype textDataType = new ConceptDatatype();
     //   textDataType.setHl7Abbreviation(hl7);
        concept.setDatatype(textDataType);
        concept.setUuid(mapping+UUID_SUFFIX);
        when(conceptService.getConceptByMapping(mapping, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME)).thenReturn(concept);
        if(answers != null) {
            concept.setAnswers(answers);
        }
        return concept;
    }

    private List<ConceptAnswer> dispositionAnswers(){
        List<ConceptAnswer> answers = new ArrayList<ConceptAnswer>();

        String[] dispositionAnswers = {"ADMIT","DISCHARGE","TRANSFER"};

        for (String dispositionAnswer : dispositionAnswers) {
            ConceptAnswer conceptAnswer = new ConceptAnswer();
            Concept dispositionAnsConcept = new Concept();
            dispositionAnsConcept.setUuid(dispositionAnswer + UUID_SUFFIX);
            conceptAnswer.setAnswerConcept(dispositionAnsConcept);

            answers.add(conceptAnswer);
        }
        return answers;
    }
}
