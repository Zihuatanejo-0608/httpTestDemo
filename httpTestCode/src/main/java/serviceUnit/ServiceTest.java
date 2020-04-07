package serviceUnit;

import cn.hutool.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import toolsUnit.CSVData;
import toolsUnit.HuToolHttpUtil;
import toolsUnit.ToolsUnit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by andy on 2020/4/7.
 *
 * 停止模型服务
 */
public class ServiceTest {
    static JSONObject jsonObject= ToolsUnit.getBaseData("baseData.json");
    String token;
    String headerName = jsonObject.get("headerName").toString();
    static String urlHead = jsonObject.get("urlHead").toString();
    String str = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    Map<String,String> dataA;
    Map<String,String> dataB;

    @BeforeClass(alwaysRun = true)
    public void initData() throws Exception {
        token = InitLogin.loginApi(jsonObject.get("ckm").toString(), jsonObject.get("ckmPwd").toString());
        dataA = InitServiceData.createServingTaskData(4, headerName, token, "adult.pmml", "pmml");//running
        dataB = InitServiceData.createServingTaskData(5, headerName, token, "adult.pmml", "pmml");//stop
    }

    @DataProvider(name = "ServingTaskTestStop")
    public static Iterator<Object[]> testData() throws Exception{
        return (Iterator<Object[]>) new CSVData("ServingTaskTestStop.csv");
    }

    @Test(dataProvider = "ServingTaskTestStop")
    public void stop(Map<String,String> map) throws Exception{
        String detailId;
        switch (map.get("detailId")){
            case "running":
                detailId = dataA.get("detailId");
                break;
            case "stop":
                detailId = dataB.get("detailId");
                break;
            default:
                detailId = map.get("detailId");
                break;
        }
        String url = urlHead + "serving/task/stop?detailId=" + detailId;
        JSONObject response = HuToolHttpUtil.get(url, headerName, token);
        String actualMessage = (String) response.get("message");
        System.out.println("预期结果:" + map.get("message") + "\n" + "实际结果:" + actualMessage);
        //断言
        Assert.assertEquals(actualMessage,map.get("message"));//实际结果,预期结果

    }

    @AfterClass
    public void after() throws Exception{
        InitServiceData.batchProcessServingModelData(headerName,token);
        Thread.sleep(10000);
        //批量删除模型
        InitServiceData.deleteModel(headerName,token);
    }





}
