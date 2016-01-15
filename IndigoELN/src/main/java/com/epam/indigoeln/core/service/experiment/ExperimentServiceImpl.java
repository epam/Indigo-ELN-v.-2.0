package com.epam.indigoeln.core.service.experiment;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.web.rest.dto.ExperimentTablesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class ExperimentServiceImpl implements ExperimentService {

    @Autowired
    private ExperimentRepository experimentRepository;

    @Override
    public Experiment save(Experiment experiment) {
        return experimentRepository.save(experiment);
    }

    @Override
    public List<Experiment> findAll() {
        return experimentRepository.findAll();
    }

    @Override
    public Experiment findOne(String id) {
        return experimentRepository.findOne(id);
    }

    @Override
    public Collection<Experiment> findByAuthor(User author) {
        return experimentRepository.findByAuthor(author);
    }

    @Override
    public void delete(String id) {
        experimentRepository.delete(id);
    }

    @Override
    public ExperimentTablesDTO getExperimentTables() {
        ExperimentTablesDTO dto = new ExperimentTablesDTO();
        User author = new User();
        author.setLogin("UserAuthor");
        User coAuthor = new User();
        coAuthor.setLogin("UserCoAuthor");
        User witness = new User();
        witness.setLogin("UserWitness");

        List<User> witnessList = new ArrayList<>();
        List<User> coauthorsList = new ArrayList<>();

        witnessList.add(witness);
        coauthorsList.add(coAuthor);
        coauthorsList.add(witness);

        Experiment experiment1 = new Experiment();
        experiment1.setId("AAA111");
        experiment1.setTitle("Elixir of immortality");
        experiment1.setAuthor(author);
        experiment1.setCoAuthors(coauthorsList);
        experiment1.setComments("Awesome Experiment 1");
        experiment1.setCreationDate(LocalDate.now());
        experiment1.setLastEditDate(LocalDate.now());
        experiment1.setLastModifiedBy(author);
        experiment1.setProject("Cool Project 1");
        experiment1.setStatus("Completed");
        experiment1.setWitness(witnessList);

        Experiment experiment2 = new Experiment();
        experiment2.setId("AAA112");
        experiment2.setTitle("Potion of happiness");
        experiment2.setAuthor(author);
        experiment2.setCoAuthors(coauthorsList);
        experiment2.setComments("Awesome Experiment 2");
        experiment2.setCreationDate(LocalDate.now());
        experiment2.setLastEditDate(LocalDate.now());
        experiment2.setLastModifiedBy(author);
        experiment2.setProject("Cool Project 2");
        experiment2.setStatus("Submitted");
        experiment2.setWitness(witnessList);

        Experiment experiment3 = new Experiment();
        experiment3.setId("AAA113");
        experiment3.setTitle("Potion of joy");
        experiment3.setAuthor(author);
        experiment3.setCoAuthors(coauthorsList);
        experiment3.setComments("Awesome Experiment 3");
        experiment3.setCreationDate(LocalDate.now());
        experiment3.setLastEditDate(LocalDate.now());
        experiment3.setLastModifiedBy(author);
        experiment3.setProject("Cool Project 3");
        experiment3.setStatus("Archived");
        experiment3.setWitness(witnessList);

        Experiment experiment4 = new Experiment();
        experiment4.setId("AAA114");
        experiment4.setTitle("Elixir of time");
        experiment4.setAuthor(author);
        experiment4.setCoAuthors(coauthorsList);
        experiment4.setComments("Awesome Experiment 4");
        experiment4.setCreationDate(LocalDate.now());
        experiment4.setLastEditDate(LocalDate.now());
        experiment4.setLastModifiedBy(author);
        experiment4.setProject("Cool Project 4");
        experiment4.setStatus("Signing");
        experiment4.setWitness(witnessList);

        Experiment[] set1 = {experiment1, experiment2, experiment3, experiment4};
        Experiment[] set2 = {experiment1, experiment2, experiment3};
        Experiment[] set3 = {experiment3, experiment4};

        dto.setOpenAndCompletedExp(Arrays.asList(set1));
        dto.setWaitingSignatureExp(Arrays.asList(set2));
        dto.setSubmittedAndSigningExp(Arrays.asList(set3));
        return dto;
    }

}
