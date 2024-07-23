/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.repository.registration;

import com.epam.indigoeln.core.model.Compound;
import com.epam.indigoeln.core.model.CompoundTableRowInfo;

import java.util.List;

public interface RegistrationRepository {

    RegistrationRepositoryInfo getInfo();

    String register(List<Compound> compounds) throws RegistrationException;

    RegistrationStatus getRegisterJobStatus(String jobId) throws RegistrationException;

    List<Compound> getRegisteredCompounds(String jobId) throws RegistrationException;

    List<Integer> searchExact(String structure, String searchOption) throws RegistrationException;

    List<Integer> searchSub(String structure, String searchOption) throws RegistrationException;

    List<Integer> searchSmarts(String structure) throws RegistrationException;

    List<Integer> searchSim(String structure, String similarity, Double var3, Double var4) throws RegistrationException;

    List<CompoundTableRowInfo> getCompoundInfoByCompoundNo(String compoundNo) throws RegistrationException;

    List<Compound> getCompoundInfoByCasNo(String casNo) throws RegistrationException;

    Compound getCompoundInfoByBatchNo(String batchNo) throws RegistrationException;

}
