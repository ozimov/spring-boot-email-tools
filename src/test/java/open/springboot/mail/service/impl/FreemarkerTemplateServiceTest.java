package open.springboot.mail.service.impl;

import com.google.common.collect.ImmutableMap;
import open.springboot.mail.service.TemplateService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import testutils.TestApplication;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Map;

import static it.ozimov.cirneco.hamcrest.java7.AssertFluently.given;
import static junit.framework.TestCase.fail;
import static open.springboot.mail.service.impl.TemplatingTestUtils.NAME;
import static open.springboot.mail.service.impl.TemplatingTestUtils.TEMPLATE;
import static open.springboot.mail.service.impl.TemplatingTestUtils.getExpectedBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
@WebIntegrationTest("server.port=0")
public class FreemarkerTemplateServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Autowired
    private TemplateService templateService;

    @Test
    public void testMergeTemplateIntoString() throws Exception {
        //Arrange
        final String expectedBody = getExpectedBody();

        //Act
        final String body = templateService.mergeTemplateIntoString(TemplatingTestUtils.TEMPLATE, TemplatingTestUtils.MODEL_OBJECT);

        //Assert
        given(body).assertThat(is(expectedBody));
    }

    @Test
    public void testCannotAcceptEmptyTemplateName() throws Exception {
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
    public void testCannotAcceptNonFreemarkerTemplateName() throws Exception {
        //Arrange
        final Map<String, Object> modelObject = new ImmutableMap.Builder<String, Object>()
                .build();
        expectedException.expect(IllegalArgumentException.class);

        //Act
        templateService.mergeTemplateIntoString("file.html", modelObject);

        //Assert
        fail("IllegalArgumentException expected");
    }

}