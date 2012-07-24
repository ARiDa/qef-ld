import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import br.ufc.lia.qef.datasource.SparqlEndpointDataSourceTest;
import br.ufc.lia.qef.sparql.JoinQueryManipulationTest;


@RunWith(Suite.class)
@SuiteClasses({SparqlEndpointDataSourceTest.class, JoinQueryManipulationTest.class})

public class TestSuite {

}
