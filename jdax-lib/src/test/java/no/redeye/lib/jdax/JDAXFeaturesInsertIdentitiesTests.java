package no.redeye.lib.jdax;

import java.io.IOException;
import java.sql.SQLException;
import no.redeye.lib.jdax.types.InsertResults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 */
@ExtendWith(MockitoExtension.class)
public class JDAXFeaturesInsertIdentitiesTests extends JDAXFeaturesTestBase {

    @AfterEach
    public void tearDown() {
        tearDownDS();
    }

    @Test
    @DisplayName("Returns identity fields when USE_GENERATED_KEYS_FLAG is set")
    public void givenGeneratedKeysEnabled_thenIdentityFieldsReturned() throws SQLException, IOException {

        String testTable = "givenGeneratedKeysEnabled_thenIdentityFieldsReturned";

        InsertResults inserts = setUpTest(
                TEST_RECORD_ALL_VALUES,
                toTestQuery(INSERT_FULL_RECORD, testTable),
                testTable,
                Features.USE_GENERATED_KEYS_FLAG);

        Assertions.assertEquals(1, inserts.count());
        Assertions.assertTrue(inserts.hasIdentities());
        Assertions.assertNotNull(inserts.type(0));
        Assertions.assertEquals(1L, inserts.longIdentity(0).longValue());
        Assertions.assertEquals("1", inserts.stringIdentity(0));
    }

    @Test
    @DisplayName("Expect DB to throw exception when requesting identity fields without USE_GENERATED_KEYS_FLAG")
    public void givenNoGeneratedKeysEnabled_thenIdentityFieldsFail() throws SQLException, IOException {

        String testTable = "givenNoGeneratedKeysEnabled_thenIdentityFieldsFail";
            InsertResults inserts = setUpTest(
                    TEST_RECORD_ALL_VALUES,
                    toTestQuery(INSERT_FULL_RECORD, testTable),
                    testTable);
        Assertions.assertEquals(1, inserts.count());
        Assertions.assertFalse(inserts.hasIdentities());
        Assertions.assertNull( inserts.longIdentity(0));
    }

}
