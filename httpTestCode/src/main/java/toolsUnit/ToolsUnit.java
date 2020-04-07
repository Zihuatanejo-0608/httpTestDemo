package toolsUnit;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Created by andy on 2019/6/28.
 */
public class ToolsUnit {

    public static String randomData(){
        try{
            int r = (int)((Math.random()*9+1)*100000000);
            return String.valueOf(r);
        }catch (Exception e){
            System.out.println("随机数生成失败!");
            return null;
        }
    }

    public static String filePath(String fileName) throws Exception{
        File file = new File(".");
        String path = ".src.main.resources.";
        String absolutePath = file.getCanonicalPath() + path.replaceAll("\\.", Matcher.quoteReplacement("\\")) + fileName;
        System.out.println("资源数据路径:" +absolutePath);

        return absolutePath;
    }

    public static void writeTxt(String file,List<String> list) throws Exception{
        String path = ToolsUnit.filePath(file);

        FileUtil.writeLines(list,path, Charset.defaultCharset(),true);
        System.out.println("写入" + file + "成功!");

    }

    public static String getPath(String fileName) throws Exception {
        String absolutePath = FileUtil.getAbsolutePath(fileName);
        System.out.println("数据资源路径:" + absolutePath);
        return absolutePath;
    }

    public static String getTxtStr(String fileName) throws Exception{
        String absolutePath = FileUtil.getAbsolutePath(fileName);
        String str = FileUtil.readString(absolutePath, Charset.defaultCharset());
        System.out.println(str);
//        JSON json = JSONUtil.readJSON(new File(absolutePath), Charset.defaultCharset());
//        System.out.println(json.toString());
        return str;
    }

    //通过文件流解析文件数据
    public static Map<String,String> getBaseData_txt(String fileName){

        try{
            List<String> list = new ArrayList();
            int rowNum = 0;//行数

            //把文件变成流对象
            InputStream inputStream = ToolsUnit.class.getResourceAsStream("/" + fileName);
            //读取流对象
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            //遍历流对象,添加到list
            while (bufferedReader.ready()){
                list.add(bufferedReader.readLine());
                rowNum++;
            }

            Map<String, String> map = list.stream().collect(Collectors.toMap(s -> s.split("=")[0], s -> s.split("=")[1]));

            return map;
        }catch (Exception e){
            e.getMessage();
            System.out.println("获取文件数据异常!");
        }
        return null;
    }

    //获取json文件数据
    public static JSONObject getBaseData(String fileName){
        try{
            //把文件变成流对象
            InputStream inputStream = ToolsUnit.class.getResourceAsStream("/" + fileName);
            //读取流,转换成String
            String readStream = IoUtil.read(inputStream, Charset.defaultCharset());

            JSONObject jsonObject = new JSONObject(readStream);

            return jsonObject;
        }catch (Exception e){
            e.getMessage();
            System.out.println("获取文件数据异常!");
        }
        return null;
    }






}
