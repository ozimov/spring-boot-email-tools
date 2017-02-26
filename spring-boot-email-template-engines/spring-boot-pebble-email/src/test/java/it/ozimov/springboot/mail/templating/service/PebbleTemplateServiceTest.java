/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.ozimov.springboot.mail.templating.service;

import com.google.common.collect.ImmutableMap;
import com.mitchellbosecke.pebble.error.PebbleException;
import it.ozimov.springboot.mail.PebbleTestApplication;
import it.ozimov.springboot.mail.service.TemplateService;
import it.ozimov.springboot.mail.service.exception.TemplateException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.UUID;

import static it.ozimov.cirneco.hamcrest.java7.AssertFluently.given;
import static junit.framework.TestCase.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PebbleTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PebbleTemplateServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private TemplateService templateService;

    @Test
    public void shouldMergeTemplateIntoString() throws Exception {
        //Arrange
        final String expectedBody = TemplatingTestUtils.getExpectedBody();

        //Act
        final String body = templateService.mergeTemplateIntoString(TemplatingTestUtils.TEMPLATE,
                TemplatingTestUtils.MODEL_OBJECT);

        //Assert
        given(body).assertThat(is(expectedBody));
    }

    @Test
    public void shouldNotAcceptEmptyTemplateName() throws Exception {
        //Arrange
        final Map<String, Object> modelObject = new ImmutableMap.Builder<String, Object>()
                .build();
        expectedException.expect(IllegalArgumentException.class);

        //Act
        templateService.mergeTemplateIntoString("    ", modelObject);

        //Assert
        fail("IllegalArgumentException expected");
    }

    @Test
    public void shouldNotAcceptTemplateNameWithoutSpecifiedExtension() throws Exception {
        //Arrange
        final Map<String, Object> modelObject = new ImmutableMap.Builder<String, Object>()
                .build();
        expectedException.expect(IllegalArgumentException.class);

        //Act
        templateService.mergeTemplateIntoString("file." + UUID.randomUUID(), modelObject);

        //Assert
        fail("IllegalArgumentException expected");
    }

    @Test
    public void shouldThrowExceptionOnWrongTemplate() throws Exception {
        //Arrange
        final Map<String, Object> modelObject = new ImmutableMap.Builder<String, Object>().build();
        expectedException.expect(TemplateException.class);
        expectedException.expectCause(instanceOf(PebbleException.class));

        //Act
        templateService.mergeTemplateIntoString(TemplatingTestUtils.WRONG_TEMPLATE, modelObject);

        //Assert
        fail("TemplateException expected");
    }

}