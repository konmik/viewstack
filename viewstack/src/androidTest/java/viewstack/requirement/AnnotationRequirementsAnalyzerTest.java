package viewstack.requirement;

import android.test.UiThreadTest;

import testkit.BaseActivityTest;
import testkit.TestActivity;

import static java.util.Arrays.asList;

public class AnnotationRequirementsAnalyzerTest extends BaseActivityTest<TestActivity> {

    public AnnotationRequirementsAnalyzerTest() {
        super(TestActivity.class);
    }

    class View1 {
    }

    @RequiredViews(required = View1.class)
    class View2 {
    }

    @RequiredViews(required = View2.class, visible = View2.class)
    class View3 {
    }

    @UiThreadTest
    public void testAnalyze() throws Exception {
        AnnotationRequirementsAnalyzer analyzer = new AnnotationRequirementsAnalyzer();
        RequirementsAnalyzer.Analysis analysis = analyzer.analyze(asList(new Class[]{View1.class, View2.class, View3.class}));
        assertEquals(3, analysis.required);
        assertEquals(2, analysis.visible);

        analysis = analyzer.analyze(asList(new Class[]{View1.class, View2.class, View1.class, View3.class}));
        assertEquals(4, analysis.required);
        assertEquals(3, analysis.visible);

        analysis = analyzer.analyze(asList(new Class[]{View1.class}));
        assertEquals(1, analysis.required);
        assertEquals(1, analysis.visible);
    }
}
