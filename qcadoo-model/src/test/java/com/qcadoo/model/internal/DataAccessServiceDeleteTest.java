/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 0.4.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.model.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.model.api.types.HasManyType;
import com.qcadoo.model.beans.sample.SampleParentDatabaseObject;
import com.qcadoo.model.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.model.internal.types.HasManyEntitiesType;

public class DataAccessServiceDeleteTest extends DataAccessTest {

    @Test
    public void shouldProperlyDelete() throws Exception {
        // given
        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);

        given(session.get(SampleSimpleDatabaseObject.class, 1L)).willReturn(simpleDatabaseObject);

        // when
        dataDefinition.delete(1L);

        // then
        verify(session).delete(simpleDatabaseObject);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailIfEntityNotFound() throws Exception {
        // given
        given(session.get(SampleSimpleDatabaseObject.class, 1L)).willReturn(null);

        // when
        dataDefinition.delete(1L);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldProperlyDeleteAndNullifyChildren() throws Exception {
        // given
        SampleParentDatabaseObject parentDatabaseEntity = new SampleParentDatabaseObject(1L);
        parentDatabaseEntity.setName("Mr X");
        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);
        simpleDatabaseObject.setBelongsTo(parentDatabaseEntity);
        List<SampleSimpleDatabaseObject> entities = new ArrayList<SampleSimpleDatabaseObject>();
        entities.add(simpleDatabaseObject);
        parentFieldDefinitionHasMany.withType(new HasManyEntitiesType("simple", "entity", "belongsTo",
                HasManyType.Cascade.NULLIFY, false, dataDefinitionService));
        parentDataDefinition.withField(parentFieldDefinitionHasMany);

        given(session.get(SampleParentDatabaseObject.class, 1L)).willReturn(parentDatabaseEntity);
        given(hibernateService.getTotalNumberOfEntities(Mockito.any(Criteria.class))).willReturn(1);
        given(hibernateService.list(Mockito.any(Criteria.class))).willReturn((List) entities);
        given(session.get(SampleSimpleDatabaseObject.class, 1L)).willReturn(simpleDatabaseObject);

        // when
        parentDataDefinition.delete(1L);

        // then
        verify(session).save(simpleDatabaseObject);
        verify(session).delete(parentDatabaseEntity);
    }

}
