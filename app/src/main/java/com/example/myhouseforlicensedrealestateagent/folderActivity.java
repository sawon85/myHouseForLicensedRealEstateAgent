package com.example.myhouseforlicensedrealestateagent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class folderActivity extends Activity {


    String mCurrent;
    String mRoot;
    TextView mCurrentTxt;
    ListView mFileList;
    ArrayAdapter<String> mAdapter;
    ArrayList<String> arFiles;
    ArrayList<coordinateVO> coordinateList = new ArrayList<coordinateVO>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
        mCurrentTxt = (TextView)findViewById(R.id.current);
        mFileList = (ListView)findViewById(R.id.filelist);

        arFiles = new ArrayList<String>();

        //SD카드 루트 가져옴
        mRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        mCurrent = mRoot + "/Download";

        //어댑터를 생성하고 연결해줌
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arFiles);

        mFileList.setAdapter(mAdapter);//리스트뷰에 어댑터 연결
        mFileList.setOnItemClickListener(mItemClickListener);//리스너 연결

        refreshFiles();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        NetworkTask networkTask = null;
        String url = "http://106.10.35.170/StoreHomeInfo.php";

        File initFile = null;
        try {
            initFile = new File(AppManager.getInstance().getExcelPath());
            InputStream is = new FileInputStream(initFile);

            Workbook wb = Workbook.getWorkbook(is);
            if(wb != null){
                Sheet sheet = wb.getSheet(0); // 시트 불러오기
                if(sheet != null) {
                    int colTotal = sheet.getColumns(); //전체 컬럼
                    int rowIndexStart = 1;              // row 인덱스 시작
                    int rowTotal = sheet.getColumn(colTotal -1 ).length;

                    for(int row = rowIndexStart; row < rowTotal ; row++){

                        String home_index = sheet.getCell(23, row).getContents();
                        String type = sheet.getCell(3, row).getContents();
                        String spacious1 = sheet.getCell(4, row).getContents();
                        String spacious2 = sheet.getCell(5, row).getContents();
                        String dong = sheet.getCell(7, row).getContents();
                        String lot_number = sheet.getCell(8, row).getContents();
                        String name = sheet.getCell(9, row).getContents();
                        String address1 = sheet.getCell(10, row).getContents();
                        String address2 = sheet.getCell(11, row).getContents();
                        String floor = sheet.getCell(12, row).getContents();
                        String price = sheet.getCell(14, row).getContents();
                        String deposit = sheet.getCell( 15, row).getContents();
                        String monthly_rent = sheet.getCell(16, row).getContents();
                        String loan = sheet.getCell(17, row).getContents();
                        String information = sheet.getCell(18, row).getContents();
                        String feature = sheet.getCell(19, row).getContents();
                        String transmit_date = sheet.getCell(21, row).getContents();
                        String registration_date = sheet.getCell(22, row).getContents();

                        String longitude = Double.toString(coordinateList.get(row-1).longitude);
                        String latitude = Double.toString(coordinateList.get(row-1).latitude);

                        String data = getData(home_index, type, spacious1, spacious2, dong, lot_number, latitude, longitude, name, address1, address2,
                                floor, price, deposit, monthly_rent, loan, information, feature, transmit_date, registration_date);
                        Log.e("data", data);

                        networkTask = new NetworkTask(folderActivity.this, url, data, 1);
                        networkTask.execute();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }




    //리스트뷰 클릭 리스너
    AdapterView.OnItemClickListener mItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // TODO Auto-generated method stub
                    String Name = arFiles.get(position);//클릭된 위치의 값을 가져옴

                    //디렉토리이면
                    if(Name.startsWith("[") && Name.endsWith("]")){
                        Name = Name.substring(1, Name.length() - 1);//[]부분을 제거해줌
                    }

                    //들어가기 위해 /와 터치한 파일 명을 붙여줌
                    String Path = mCurrent + "/" + Name;
                    File f = new File(Path);//File 클래스 생성

                    if(f.isDirectory()){//디렉토리면?
                        mCurrent = Path;//현재를 Path로 바꿔줌
                        refreshFiles();//리프레쉬
                    }else{
                        //디렉토리가 아니면 토스트 메세지를 뿌림
                        if(Name.contains(".xls"))
                        {
                            AppManager.getInstance().setExcelPath(mCurrent + "/" + Name);

                            File initFile = null;

                            try {
                                initFile = new File(AppManager.getInstance().getExcelPath());
                                InputStream is = new FileInputStream(initFile);

                                Workbook wb = Workbook.getWorkbook(is);
                                if(wb != null){
                                    Sheet sheet = wb.getSheet(0); // 시트 불러오기
                                    if(sheet != null) {
                                        int colTotal = sheet.getColumns(); //전체 컬럼
                                        int rowIndexStart = 1;              // row 인덱스 시작
                                        int rowTotal = sheet.getColumn(colTotal -1 ).length;

                                        for(int row = rowIndexStart; row < rowTotal ; row++){

                                            String dong = sheet.getCell(7, row).getContents();
                                            final String lot_number = sheet.getCell(8, row).getContents();
                                            final String deleteDong = DeleteParenthesis(dong);

                                            new Thread() {
                                                String result = null;

                                                public void run() {
                                                    result = getLocation(deleteDong +" "+lot_number);

                                                    Bundle bun = new Bundle();
                                                    bun.putString("result", result);

                                                    Message msg = handler.obtainMessage();
                                                    msg.setData(bun);
                                                    handler.sendMessage(msg);
                                                }
                                            }.start();

                                        }
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (BiffException e) {
                                e.printStackTrace();
                            }




                            finish();
                        }
                    }
                }
            };



    //버튼 2개 클릭시
    public void mOnClick(View v){
        switch(v.getId()){
            case R.id.btnroot://루트로 가기
                if(mCurrent.compareTo(mRoot) != 0){
                    //루트가 아니면 루트로 가기
                    mCurrent = mRoot;
                    refreshFiles();//리프레쉬
                }
                break;
            case R.id.btnup:
                if(mCurrent.compareTo(mRoot) != 0){//루트가 아니면
                    int end = mCurrent.lastIndexOf("/");///가 나오는 마지막 인덱스를 찾고
                    String uppath = mCurrent.substring(0, end);//그부분을 짤라버림 즉 위로가게됨
                    mCurrent = uppath;
                    refreshFiles();//리프레쉬
                }
                break;
        }
    }

    public String getData(String home_index, String type, String spacious1, String spacious2, String dong, String lot_number,  String latitude, String longitude,String name, String address1, String address2, String floor,
                          String price, String deposit, String monthly_rent, String loan, String information, String feature, String transmit_date, String registration_date){
        String data = null;

        final String deleteDong = DeleteParenthesis(dong);

        data = "home_index=" + home_index + "&type=" + type + "&spacious1=" + spacious1 + "&spacious2=" + spacious2 + "&dong=" + deleteDong + "&lot_number=" + lot_number +"&latitude=" + latitude  + "&longitude=" + longitude + "&name=" + name +
                "&address1=" + address1 + "&address2=" + address2 + "&floor=" + floor + "&price=" + price + "&deposit=" + deposit + "&monthly_rent=" +monthly_rent + "&loan=" + loan +
                "&information=" + information + "&feature=" + feature + "&transmit_date=" + transmit_date + "&registration_date=" + registration_date;

        return data;
    }


    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            Bundle bun = msg.getData();
            String location = bun.getString("result");
            String location1 = location;
            String location2 = location;

            int index = location.indexOf(" ");

            String y = location1.substring(0, index);
            String x = location2.substring(index);

            double longitude = Double.parseDouble(y);
            double latitude = Double.parseDouble(x);

            coordinateList.add(new coordinateVO(longitude, latitude));
        }
    };


    public String DeleteParenthesis(String dong){
        String deleteDong = null;

        int location = dong.indexOf('(');
        if(location != -1){

            deleteDong = dong.substring(0, location);
        }

        return deleteDong;
    }


    public String getLocation(String address){
        URL url = null;
        String result = null;
        try {
            url = new URL("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode" + "?query=" + address);
        } catch (
                MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("GET");
            huc.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "w6pg36t959");
            huc.setRequestProperty("X-NCP-APIGW-API-KEY", "ISfgEqfV5XXAc74DQ2FqQgWkLXkHqS11gDUlI6i2");
            int requestCode = huc.getResponseCode();
            BufferedReader reader;
            if (requestCode == 200) {
                reader = new BufferedReader(new InputStreamReader(huc.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(huc.getErrorStream()));
            }

            String inputLine;
            StringBuffer buffer = new StringBuffer();
            while ((inputLine = reader.readLine()) != null) {
                buffer.append(inputLine);
            }
            if (reader != null)
                reader.close();

            result = buffer.toString();
            JSONObject jsonObject = new JSONObject(result);
            JSONArray addressString = jsonObject.getJSONArray("addresses");
            //JSONObject addressJson = new JSONObject(addressString);
            double x = Double.parseDouble(addressString.getJSONObject(0).getString("x"));
            double y = Double.parseDouble(addressString.getJSONObject(0).getString("y"));

            //Log.e("결과", "x : " + x + "\ny : " + y);
            result = x + " " + y;
        } catch (
                IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }



    void refreshFiles(){
        mCurrentTxt.setText(mCurrent);//현재 PATH를 가져옴
        arFiles.clear();//배열리스트를 지움

        File current = new File(mCurrent);//현재 경로로 File클래스를 만듬
        String[] files = current.list();//현재 경로의 파일과 폴더 이름을 문자열 배열로 리턴

        //파일이 있다면?
        if(files != null){
            //여기서 출력을 해줌
            for(int i = 0; i < files.length;i++){
                String Path = mCurrent + "/" + files[i];
                String Name = "";

                File f = new File(Path);

                if(f.isDirectory()){
                    Name = "[" + files[i] + "]";//디렉토리면 []를 붙여주고
                }else{
                    Name = files[i];//파일이면 그냥 출력
                }
                arFiles.add(Name);//배열리스트에 추가해줌
            }
        }
        //다끝나면 리스트뷰를 갱신시킴
        mAdapter.notifyDataSetChanged();

    }

}
