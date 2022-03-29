package org.woheller69.spritpreise;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.woheller69.spritpreise.database.DatabaseTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({DatabaseTest.class})
public class AndroidTestSuite {
}
