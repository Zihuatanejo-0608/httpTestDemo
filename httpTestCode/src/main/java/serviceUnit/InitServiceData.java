package serviceUnit;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.testng.annotations.Test;
import toolsUnit.HuToolHttpUtil;
import toolsUnit.ToolsUnit;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by andy on 2020/4/7.
 *
 *
 */
public class InitServiceData {
    static JSONObject jsonObject= ToolsUnit.getBaseData("baseData.json");
    static String urlHead = jsonObject.get("urlHead").toString();

    //1.创建一个模型
    public static String addModelData(String mName,String type,String headerName,String token) throws Exception{
        String url = urlHead + "serving/model/add";
        JSONObject request = new JSONObject();
        //模型名字,类型,描述
        request.put("name", mName);
        request.put("type",type);//dict_image_info表image_name字段
        request.put("description", "dataInit");
        JSONObject respone = HuToolHttpUtil.post(url, headerName, token, request);
        String data = (String)respone.get("data");
        return data;
    }

    //2.根据模型id创建一个版本
    public static String addVersionsData(String vName,String modelId,String headerName,String token) throws Exception{
        String url = urlHead + "serving/version/add";
        JSONObject request = new JSONObject();
        //版本名称,模型ID,描述
        request.put("name",vName);
        request.put("modelId",modelId);
        request.put("description","ckmTest");
        JSONObject respone = HuToolHttpUtil.post(url, headerName, token, request);
        String data = (String)respone.get("data");
        return data;
    }

    //3.根据模型id查询出版本路径
    public static String findVersionsData(String modelId,String headerName,String token) throws Exception{
        String url = urlHead + "serving/version/find?modelId=" + modelId;
        JSONObject respone = HuToolHttpUtil.get(url, headerName, token);
        JSONArray list = respone.getJSONArray("data");
        List<Object> fileObject = list.stream().filter(l -> new JSONObject(l.toString()).get("fileObject") != null).collect(Collectors.toList());
        if (fileObject.isEmpty()){
            return null;
        }
        Object data = fileObject.get(0);
        System.out.println(data.toString());
        String string = new JSONObject(data.toString()).getJSONObject("fileObject").getStr("id");
        System.out.println(string);

        return string;
    }

    //4.上传文件,5M+
    public static boolean uploaderVersionData(String fileName,String path,String headerName,String token) throws Exception{
        String url = urlHead + "serving/version/upload";
        String file = ToolsUnit.filePath(fileName);
        HttpResponse httpResponse = HttpUtil.createPost(url).header(headerName,token).form("file", FileUtil.file(file)).form("path", path).execute();
        String body = httpResponse.body();
        System.out.println(body);
        return new cn.hutool.json.JSONObject(body).getInt("code") == 200;
    }

    //5.创建一个模型服务
    public static JSONObject create(String versionId,String fileName,String headerName,String token) throws Exception{
        String url = urlHead + "serving/task/create";

        int imageId = 0 ;
        //serving_online_image_info表id字段,1=op,2=ONIX,3=pmml
        switch (fileName){
            case "adult.pmml":
                imageId = 3;
                break;
            case "ONNX.zip":
                imageId = 2;
                break;
            case "op.zip":
                imageId = 1;
                break;
        }

        JSONObject request = new JSONObject();

        request.put("cpu",1);
        request.put("gpu",0);
        request.put("mem",2);
        request.put("replicas",1);//实例数,K8S上检查N个pod

        request.put("versionId",Long.valueOf(versionId));//modelData.get("vid")
        request.put("imageId",imageId);
        request.put("name","s" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        request.put("description","ckmTest");

        JSONObject ONIX = new JSONObject();//imageId=2
        ONIX.put("saved_type","checkpoint");
        ONIX.put("input_tensor_name","input:0");
        ONIX.put("output_tensor_name","result:0");

        JSONObject op = new JSONObject();//imageId=1
        op.put("handle_file","minist_inference.py");
        op.put("handle_class","modelService");

        switch (imageId){
            case (1):
                request.put("params",op.toString());//镜像参数,如果imageId的type=tensorflow,则根据process_type传值
                break;
            case (2):
                request.put("params",ONIX.toString());
                break;
            case (3):
                request.put("params",null);//如果imageId的type=pmml,那么不填
                break;
        }

        JSONObject respone = HuToolHttpUtil.post(url, headerName, token, request);
        JSONObject obj = (JSONObject) respone.get("data");

        return obj;//String.valueOf(obj.get("detailId"))
    }

    //停止任务,serving_online_detail_record
    public static boolean stop(String detailId,String headerName,String token) throws Exception{
        String url = urlHead + "serving/task/stop?detailId=" + detailId;
        JSONObject respone = HuToolHttpUtil.get(url, headerName, token);
        return respone.getBool("success");
    }

    public static void delete(String detailId,String headerName,String token) throws Exception{
        String url = urlHead + "serving/task/delete?detailId=" + detailId;
        HuToolHttpUtil.delete(url,headerName,token);
    }

    //8.查询任务状态
    public static String findTaskStatus(String taskId,String type,String headerName,String token) throws Exception{
        //notebook,fasttrain,serving
        String url = urlHead + "serving/findTaskStatus?taskId=" + taskId + "&type=" + type;
        System.out.println(url);
        JSONObject response = HuToolHttpUtil.get(url, headerName, token);
        System.out.println("taskStatus:" + response.get("data"));
        Object status = new JSONArray(response.get("data")).getJSONObject(0).get("status");
        return String.valueOf(status);
    }

    @Test
    //一条龙服务
    public static Map<String,String> createServingTaskData(int i, String headerName, String token, String fileName, String type) throws Exception{
        String modelId = null;
        String versionId = null;
        String path = null;
        boolean upload = false;
        JSONObject obj = null;
        String detailId = null;
        String taskId = null;
        String random = ToolsUnit.randomData();
        String mName = "m" + random;
        String vName = "v" + random;
        boolean stop = false;

        if (i >= 1){
            modelId = addModelData(mName,type,headerName,token);
        }
        if (i >= 2 && modelId != null){
            versionId = addVersionsData(vName,modelId,headerName,token);
        }
        if (i >= 3 && versionId != null){
            path = findVersionsData(modelId,headerName,token);
        }
        if (i >= 3 && path != null){
            upload = uploaderVersionData(fileName,path,headerName,token);
        }
        if (i >= 4 && upload){
            obj = create(versionId,fileName,headerName,token);
            detailId = String.valueOf(obj.get("detailId"));
            taskId = String.valueOf(obj.get("taskId"));
        }
        if (i >= 5 && detailId != null){
            Thread.sleep(15000);
            if (findTaskStatus(taskId,"serving",headerName,token).equals("Running")){
                stop = stop(detailId,headerName,token);
                Thread.sleep(15000);
            }
        }
        if (i >= 6 && stop){
            if (findTaskStatus(taskId,"serving",headerName,token).equals("Stop")){
                delete(detailId,headerName,token);
            }
        }
        HashMap<String, String> map = new HashMap<>(16);
        map.put("mid", modelId);
        map.put("mName", mName);
        map.put("vid", versionId);
        map.put("vName", vName);
        map.put("detailId", detailId);
        map.put("filePath",path);
        List<String> list = map.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList());
        //写入txt
        String file = "testData.txt";
        ToolsUnit.writeTxt(file,list);
        System.out.println("======================="+"一条龙服务结束"+"=======================");
        return map;

    }

    //新分页查询
    public static List<Map<String,String>> SelectTaskPage(String headerName,String token) throws Exception{
        String url = urlHead + "serving/task/selectPage";
        JSONObject request = new JSONObject();
        request.put("current",1);
        request.put("size",100);
        request.put("search",null);//搜索条件
        JSONObject respone = HuToolHttpUtil.post(url, headerName, token,request);
        JSONObject data = respone.getJSONObject("data");
        JSONArray list = data.getJSONArray("records");

        List<Map<String,String>> idList = new ArrayList<>();
        list.stream().map(JSONObject::new).map(j -> j.getJSONArray("servingOnlineDetailRecords")).map(d -> d.stream().map(o -> new JSONObject(o)).map(j -> Collections.singletonMap(j.getStr("id"),j.getStr("status"))).collect(Collectors.toList())).forEach(l -> idList.addAll(l));
        return idList;
    }

    //批量删除模型服务任务
    public static void batchProcessServingModelData(String headerName ,String token) throws Exception{
        System.out.println("===========================");
        System.out.println("开始清理model任务……");
        System.out.println("===========================");
        //查询任务列表,解析出ID,传给list
        List<Map<String, String>> maps = SelectTaskPage(headerName,token);
        //遍历list,赋值给id
        for (Map<String,String> map : maps){
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String detailId = entry.getKey();
                String status = entry.getValue();
                System.out.println("detail_id:" + detailId);

                if (status.equals("Running")){
                    //对于running 的任务,停止后删除
                    if (stop(detailId,headerName,token)){
                        delete(detailId,headerName,token);
                    }
                }else {
                    delete(detailId,headerName,token);
                }
            }
        }
    }

    //模型管理分页查询,serving_model_record
    public static List selectPage(String headerName ,String token) throws Exception{
        String url = urlHead + "serving/model/selectPage";
        JSONObject request = new JSONObject();
        request.put("current",1);//当前页
        request.put("size",100);//每页展示数
        request.put("search",null);
        JSONObject respone = HuToolHttpUtil.post(url, headerName, token, request);
        JSONObject data = respone.getJSONObject("data");
        JSONArray list = data.getJSONArray("records");
        //取出list中名字是id的集合,重新设置为list
        List<String> ids = list.stream().map(o -> new JSONObject(o).getStr("id")).collect(Collectors.toList());

        return ids;
    }

    public static void deleteModel(String headerName ,String token) throws Exception{
        List ids = selectPage(headerName,token);
        for (Object id :ids){
            String url = urlHead + "serving/model/delete?id=" + id;
            JSONObject respone = HuToolHttpUtil.delete(url, headerName, token);
            boolean success = respone.getBool("success");
            if (success == false){
                System.out.println("暂时不删除!");
            }
        }
    }





}
