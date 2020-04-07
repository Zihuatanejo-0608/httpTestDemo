package doTest;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andy on 2020/4/7.
 *
 *
 */
public class DoTest {
    public static void main(String[] args) {
        System.out.println("================== this is a test! ==================");

        try{
            //新建一个xml套件
            XmlSuite xmlSuite = new XmlSuite();
            xmlSuite.setName("autoTestSuite");//testng.xml中set

            XmlTest xmlTest = new XmlTest(xmlSuite);
            xmlTest.setName("testNGDemo");

            //类维度执行
            List<XmlClass> classes = new ArrayList<XmlClass>();
            classes.add(new XmlClass("serviceUnit.LoginTest"));
            classes.add(new XmlClass("serviceUnit.ServiceTest"));

            xmlTest.setXmlClasses(classes);

            //套件数组添加xml套件
            List<XmlSuite> suites = new ArrayList<XmlSuite>();
            suites.add(xmlSuite);

            //执行测试套件
            TestNG testng = new TestNG();
            testng.setXmlSuites(suites);
            testng.run();

        }catch (Exception e){
            System.out.println("run error!");
        }
    }
}
