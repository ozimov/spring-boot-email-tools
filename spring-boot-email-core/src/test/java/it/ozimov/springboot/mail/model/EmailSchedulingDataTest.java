package it.ozimov.springboot.mail.model;

import it.ozimov.springboot.mail.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import testutils.TestUtils;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
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

    ////////////////////////////////////////////////////////////////
    //// DEFAULT COMPARATOR TESTS
    ////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    @Test
    public void shouldDefaultComparatorCompareFirstBasedOnScheduledTime() throws Exception {
        //Arrange
        EmailSchedulingData emailSchedulingData_1 = new DummyEmailSchedulingData();
        TimeUnit.NANOSECONDS.sleep(10);
        EmailSchedulingData emailSchedulingData_2 = new DummyEmailSchedulingData();

        //Act
        int givenBeforeResult = EmailSchedulingData.DEFAULT_COMPARATOR.compare(emailSchedulingData_1, emailSchedulingData_2);
        int givenAfterResult = EmailSchedulingData.DEFAULT_COMPARATOR.compare(emailSchedulingData_2, emailSchedulingData_1);
        int givenSameResult = EmailSchedulingData.DEFAULT_COMPARATOR.compare(emailSchedulingData_1, emailSchedulingData_1);

        //Assert
        assertions.assertThat(givenBeforeResult).isEqualTo(-1);
        assertions.assertThat(givenAfterResult).isEqualTo(1);
        assertions.assertThat(givenSameResult).isEqualTo(0);
    }

    @Test
    public void shouldDefaultComparatorCompareFirstBasedOnScheduledTimeThenOnPriority() throws Exception {
        //Arrange
        final OffsetDateTime scheduledDateTime = TimeUtils.offsetDateTimeNow();

        EmailSchedulingData emailSchedulingData_1 = new DummyEmailSchedulingData().toBuilder()
                .scheduledDateTime(scheduledDateTime).assignedPriority(1).build();
        EmailSchedulingData emailSchedulingData_2 = new DummyEmailSchedulingData().toBuilder()
                .scheduledDateTime(scheduledDateTime).assignedPriority(100).build();

        assertions.assertThat(emailSchedulingData_1).hasFieldOrPropertyWithValue("scheduledDateTime", scheduledDateTime);
        assertions.assertThat(emailSchedulingData_2).hasFieldOrPropertyWithValue("scheduledDateTime", scheduledDateTime);

        //Act
        int givenBeforeResult = EmailSchedulingData.DEFAULT_COMPARATOR.compare(emailSchedulingData_1, emailSchedulingData_2);
        int givenAfterResult = EmailSchedulingData.DEFAULT_COMPARATOR.compare(emailSchedulingData_2, emailSchedulingData_1);
        int givenSameResult = EmailSchedulingData.DEFAULT_COMPARATOR.compare(emailSchedulingData_1, emailSchedulingData_1);

        //Assert
        assertions.assertThat(givenBeforeResult).isEqualTo(-1);
        assertions.assertThat(givenAfterResult).isEqualTo(1);
        assertions.assertThat(givenSameResult).isEqualTo(0);
    }


    @Test
    public void shouldDefaultComparatorCompareFirstBasedOnScheduledTimeThenOnPriorityAndFinalyOnId() throws Exception {
        //Arrange
        final OffsetDateTime scheduledDateTime = TimeUtils.offsetDateTimeNow();
        final int assignedPriority = 2;

        EmailSchedulingData emailSchedulingData_1 = new DummyEmailSchedulingData().toBuilder()
                .scheduledDateTime(scheduledDateTime).assignedPriority(assignedPriority).id("id1").build();
        EmailSchedulingData emailSchedulingData_2 = new DummyEmailSchedulingData().toBuilder()
                .scheduledDateTime(scheduledDateTime).assignedPriority(assignedPriority).id("id2").build();

        assertions.assertThat(emailSchedulingData_1).hasFieldOrPropertyWithValue("scheduledDateTime", scheduledDateTime);
        assertions.assertThat(emailSchedulingData_2).hasFieldOrPropertyWithValue("scheduledDateTime", scheduledDateTime);

        assertions.assertThat(emailSchedulingData_1).hasFieldOrPropertyWithValue("assignedPriority", assignedPriority);
        assertions.assertThat(emailSchedulingData_2).hasFieldOrPropertyWithValue("assignedPriority", assignedPriority);

        //Act
        int givenBeforeResult = EmailSchedulingData.DEFAULT_COMPARATOR.compare(emailSchedulingData_1, emailSchedulingData_2);
        int givenAfterResult = EmailSchedulingData.DEFAULT_COMPARATOR.compare(emailSchedulingData_2, emailSchedulingData_1);
        int givenSameResult = EmailSchedulingData.DEFAULT_COMPARATOR.compare(emailSchedulingData_1, emailSchedulingData_1);

        //Assert
        assertions.assertThat(givenBeforeResult).isEqualTo(-1);
        assertions.assertThat(givenAfterResult).isEqualTo(1);
        assertions.assertThat(givenSameResult).isEqualTo(0);
    }

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DummyEmailSchedulingData implements EmailSchedulingData {

        private OffsetDateTime scheduledDateTime = TimeUtils.offsetDateTimeNow();

        private int assignedPriority = 1;

        private String id = "default-id";

        @Override
        public String getId() {
            return id;
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