package it.ozimov.springboot.templating.mail.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.OffsetDateTime;

import static it.ozimov.cirneco.hamcrest.java7.AssertFluently.given;
import static it.ozimov.cirneco.hamcrest.java7.base.IsBetweenInclusive.betweenInclusive;
import static it.ozimov.cirneco.hamcrest.java7.clazz.IsValidNoArgumentConstructor.hasNoArgumentConstructor;
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.is;

@RunWith(MockitoJUnitRunner.class)
public class TimeUtilsTest {

    @Test
    public void testNowUsesUTC() throws Exception {
        //Arrange
        final long now = OffsetDateTime.now(UTC).toInstant().toEpochMilli();

        //Act
        final long actual = TimeUtils.now();

        //Assert
        given(actual).assertThat(is(betweenInclusive(now, now + 2_000)));
    }

    @Test
    public void testOffsetDateTimeNowUsesUTC() throws Exception {
        //Arrange
        final OffsetDateTime now = OffsetDateTime.now(UTC);

        //Act
        final OffsetDateTime actual = TimeUtils.offsetDateTimeNow();

        //Assert
        given(actual).assertThat(is(betweenInclusive(now, now.plusSeconds(2))));
    }

}