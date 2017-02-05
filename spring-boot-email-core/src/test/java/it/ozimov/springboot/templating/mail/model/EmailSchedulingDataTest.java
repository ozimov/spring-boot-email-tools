package it.ozimov.springboot.templating.mail.model;

import it.ozimov.springboot.templating.mail.utils.TimeUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import testutils.TestUtils;

import java.time.OffsetDateTime;
import java.util.Comparator;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmailSchedulingDataTest {

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    @Mock
    public Comparator<EmailSchedulingData> mockedComparator;

    private Comparator<EmailSchedulingData> originalComparator;

    @Before
    public void setUp() {
        originalComparator = EmailSchedulingData.DEFAULT_COMPARATOR;
    }

    @After
    public void tierDown() throws Exception {
        TestUtils.makeFinalStatic(EmailSchedulingData.class.getField("DEFAULT_COMPARATOR"), originalComparator);
    }


    @Test
    public void shouldGetDesiredPriorityReturnDefaultValue() throws Exception {
        //Arrange
        EmailSchedulingData emailSchedulingData = new DummyEmailSchedulingData();

        //Act
        int givenDesiredPriority = emailSchedulingData.getDesiredPriority();

        //Assert
        assertions.assertThat(givenDesiredPriority).isEqualTo(1);
    }

    @Test
    public void shouldGetAssignedPriorityReturnDefaultValue() throws Exception {
        //Arrange
        EmailSchedulingData emailSchedulingData = new DummyEmailSchedulingData();

        //Act
        int givenAssignedPriority = emailSchedulingData.getAssignedPriority();

        //Assert
        assertions.assertThat(givenAssignedPriority).isEqualTo(1);

    }

    @Test
    public void shouldCompareToUseDefaultComparator() throws Exception {
        //Arrange
        TestUtils.makeFinalStatic(EmailSchedulingData.class.getField("DEFAULT_COMPARATOR"), mockedComparator);

        EmailSchedulingData emailSchedulingData_1 = new DummyEmailSchedulingData();
        EmailSchedulingData emailSchedulingData_2 = new DummyEmailSchedulingData();

        int expectedCompareResult = -123;
        when(EmailSchedulingData.DEFAULT_COMPARATOR.compare(any(), any())).thenReturn(expectedCompareResult);

        //Act
        int givenCompareResult = emailSchedulingData_1.compareTo(emailSchedulingData_2);

        //Assert
        verify(EmailSchedulingData.DEFAULT_COMPARATOR).compare(emailSchedulingData_1, emailSchedulingData_2);

        assertions.assertThat(givenCompareResult).isEqualTo(expectedCompareResult);
    }

    public class DummyEmailSchedulingData implements EmailSchedulingData {

        private OffsetDateTime scheduledDateTime = TimeUtils.offsetDateTimeNow();


        @Override
        public String getId() {
            return null;
        }

        @Override
        public Email getEmail() {
            return null;
        }

        @Override
        public OffsetDateTime getScheduledDateTime() {
            return scheduledDateTime;
        }
    }

}