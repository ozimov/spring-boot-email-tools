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

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
@WebIntegrationTest("server.port=0")
public class FreemarkerTemplateServiceTest {

    private final String template = "email_template.ftl";
    private final String name = "Titus";
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Autowired
    private TemplateService templateService;

    private static String readFile(final File file) throws IOException {
        final byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, Charset.forName("UTF-8"));
    }

    @Test
    public void testMergeTemplateIntoString() throws Exception {
        //Arrange
        final Map<String, Object> modelObject = new ImmutableMap.Builder<String, Object>()
                .put("name", name)
                .build();
        final String expectedBody = getExpectedBody();

        //Act
        final String body = templateService.mergeTemplateIntoString(template, modelObject);

        //Assert
        assertThat("The template ", body, is(expectedBody));
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

    public String getExpectedBody() throws IOException {
        final File file = new File(getClass().getClassLoader()
                .getResource("templates" + File.separator + template).getFile());
        final String template = readFile(file);
        return template.replace("${name}", name);
    }
}