package com.findhouse.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bigkoo.pickerview.OptionsPickerView;
import com.findhouse.adapter.GridViewAddImgesAdpter;
import com.findhouse.data.HouseInfo;
import com.findhouse.data.JsonCity;
import com.findhouse.data.JsonData;
import com.findhouse.network.NetworkClient;
import com.findhouse.utils.GetJsonDataUtil;
import com.findhouse.utils.StringUtil;
import com.findhouse.utils.UrlUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublishActivity extends AppCompatActivity implements View.OnClickListener  {

    public static final String KEY_HOUSE = "key_house";
    public static final String KEY_FROM = "key_from";
    public static final String KEY_TYPE = "key_type";

    private boolean hasResult = false;

    private ArrayList<JsonCity> options1Items = new ArrayList<>(); //省
    private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();//市
    private ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<>();//区

    private String type = "/house";
    private String route = "/insert";

    private String title = "";
    private int price = 0;
    private String houseType = "";
    private String city = "";
    private String region = "";
    private String area = "";
    private String position = "";
    private String img = "";
    private String des = "";

    private String houseApartment = "";
    private String houseArea = "";
    private String houseFloor = "";
    private String houseFix = "";
    private String houseOrientation = "";
    private String houseInstall = "";

    private String houseDes = "";

    private String uid = "";
    private String tel = "";
    private String name = "";


    private StringUtil stringUtil = new StringUtil();

    private TextView pickCity;
    private EditText etTitle;
    private EditText etPrice;
    private EditText etArea;
    private EditText etPosition;
    private EditText etDes;
    private Button btnOk;
    private Button btnCancel;

    private GridView gw;
    private List<Map<String, Object>> datas;
    private List<String> imageUrls = new ArrayList<>();
    private List<String> newImageUrls = new ArrayList<>();
    private List<String> newImageUrlsNew = new ArrayList<>();
    private GridViewAddImgesAdpter gridViewAddImgesAdpter;
    private Dialog dialog;
    private final int PHOTO_REQUEST_CAREMA = 1;// 拍照
    private final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择private static final String PHOTO_FILE_NAME = "temp_photo.jpg";
    private File tempFile;
    private final String IMAGE_DIR = Environment.getExternalStorageDirectory() + "/gridview/";
    /* 头像名称 */
    private final String PHOTO_FILE_NAME = "temp_photo.jpg";


    private SharedPreferences share;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        share = getSharedPreferences("UserNow", Context.MODE_PRIVATE);
        uid = share.getString("uid", "");

        pickCity = findViewById(R.id.pickCity);
        etTitle = findViewById(R.id.etTitle);
        etPrice = findViewById(R.id.etPrice);
        etPosition = findViewById(R.id.etPosition);
        etArea = findViewById(R.id.etArea);
        etDes = findViewById(R.id.etDes);
        btnOk = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);

        pickCity.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        initJsonData();

        gw = (GridView) findViewById(R.id.gw);
        datas = new ArrayList<>();
        gridViewAddImgesAdpter = new GridViewAddImgesAdpter(datas, this);
        gw.setAdapter(gridViewAddImgesAdpter);
        gw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showdialog();
            }
        });

        Intent intent = this.getIntent();
        bundle = intent.getExtras();
        houseType = bundle.getString(KEY_TYPE);

    }


    private void showPickerViewCity() {// 弹出选择器（省市区三级联动）
        OptionsPickerView pvOptions = new OptionsPickerView.Builder(PublishActivity.this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                pickCity.setText(options2Items.get(options1).get(options2) + " "
                        + options3Items.get(options1).get(options2).get(options3));
                city = options2Items.get(options1).get(options2);
                region = options3Items.get(options1).get(options2).get(options3);
            }
        })
                .setTitleText("城市选择")
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                .setContentTextSize(20)
                .build();
        pvOptions.setPicker(options1Items, options2Items, options3Items);//三级选择器
        pvOptions.show();
    }

    private boolean parseJSON(String jsonData) {
        Gson gson = new Gson();
        JsonData<HouseInfo> parseResult = gson.fromJson(jsonData, new TypeToken<JsonData<HouseInfo>>(){}.getType());
        if(parseResult.getStat()!=0) {
            return false;
        }else{
            return true;
        }
    }

    private boolean parseImgJSON(String jsonData) {
        Gson gson = new Gson();
        JsonData<String> parseResult = gson.fromJson(jsonData, new TypeToken<JsonData<String>>(){}.getType());
        if(null == parseResult.getData()) {
            return false;
        }else{
            newImageUrlsNew = parseResult.getData();
            newImageUrlsNew.removeAll(newImageUrls);
            newImageUrls.addAll(newImageUrlsNew);
            return true;
        }
    }

    private void initJsonData() {//解析数据 （省市区三级联动）
        //获取assets目录下的json文件数据
        String JsonData = new GetJsonDataUtil().getJson(PublishActivity.this, "province.json");
        ArrayList<JsonCity> jsonCity = parseData(JsonData);//用Gson 转成实体
        // 添加省份数据
        options1Items = jsonCity;
        for (int i = 0; i < jsonCity.size(); i++) {//遍历省份
            ArrayList<String> CityList = new ArrayList<>();//该省的城市列表（第二级）
            ArrayList<ArrayList<String>> Province_AreaList = new ArrayList<>();//该省的所有地区列表（第三级）

            for (int c = 0; c < jsonCity.get(i).getCityList().size(); c++) {//遍历该省份的所有城市
                String CityName = jsonCity.get(i).getCityList().get(c).getName();
                CityList.add(CityName);//添加城市
                ArrayList<String> City_AreaList = new ArrayList<>();//该城市的所有地区列表

                //如果无地区数据，建议添加空字符串，防止数据为null 导致三个选项长度不匹配造成崩溃
                if (jsonCity.get(i).getCityList().get(c).getArea() == null
                        || jsonCity.get(i).getCityList().get(c).getArea().size() == 0) {
                    City_AreaList.add("");
                } else {
                    City_AreaList.addAll(jsonCity.get(i).getCityList().get(c).getArea());
                }
                Province_AreaList.add(City_AreaList);//添加该省所有地区数据
            }
            // 添加城市数据
            options2Items.add(CityList);
            // 添加地区数据
            options3Items.add(Province_AreaList);
        }
    }

    private ArrayList<JsonCity> parseData(String result) {//Gson 解析
        ArrayList<JsonCity> detail = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(result);
            Gson gson = new Gson();
            for (int i = 0; i < data.length(); i++) {
                JsonCity entity = gson.fromJson(data.optJSONObject(i).toString(), JsonCity.class);
                detail.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return detail;
    }

    /**
     * 选择图片对话框
     */
    public void showdialog() {
        View localView = LayoutInflater.from(this).inflate(
                R.layout.dialog_add_picture, null);
        TextView tv_gallery = (TextView) localView.findViewById(R.id.tv_gallery);
        TextView tv_cancel = (TextView) localView.findViewById(R.id.tv_cancel);
        dialog = new Dialog(this);
        dialog.setContentView(localView);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        // 设置全屏
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = display.getWidth(); // 设置宽度
        dialog.getWindow().setAttributes(lp);
        dialog.show();
        tv_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });


        tv_gallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // 从系统相册选取照片
                gallery();
//                intent = new Intent(Intent.ACTION_PICK, null);
//                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//                startActivityForResult(intent, CameraGallaryUtil.PHOTO_REQUEST_GALLERY);
            }
        });
    }

    /**
     * 判断sdcard是否被挂载
     */
    public boolean hasSdcard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }


    /**
     * 从相册获取
     */
    public void gallery() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PHOTO_REQUEST_GALLERY) {
                // 从相册返回的数据
                if (data != null) {
                    // 得到图片的全路径
                    Uri uri = data.getData();
                    String[] proj = {MediaStore.Images.Media.DATA};
                    //好像是android多媒体数据库的封装接口，具体的看Android文档
                    Cursor cursor = managedQuery(uri, proj, null, null, null);
                    //按我个人理解 这个是获得用户选择的图片的索引值
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    //将光标移至开头 ，这个很重要，不小心很容易引起越界
                    cursor.moveToFirst();
                    //最后根据索引值获取图片路径
                    String path = cursor.getString(column_index);
                    uploadImage(path);
                }

            } else if (requestCode == PHOTO_REQUEST_CAREMA) {
                if (resultCode != RESULT_CANCELED) {
                    // 从相机返回的数据
                    if (hasSdcard()) {
                        if (tempFile != null) {
                            uploadImage(tempFile.getPath());
                        } else {
                            Toast.makeText(this, "相机异常请稍后再试！", Toast.LENGTH_SHORT).show();
                        }

                        Log.i("images", "拿到照片path=" + tempFile.getPath());
                    } else {
                        Toast.makeText(this, "未找到存储卡，无法存储照片！", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0xAAAAAAAA) {
                photoPath(msg.obj.toString());
            }

        }
    };

    /**
     * 上传图片
     *
     * @param path
     */
    private void uploadImage(final String path) {
        new Thread() {
            @Override
            public void run() {
                if (new File(path).exists()) {
                    Log.d("images", "源文件存在" + path);
                } else {
                    Log.d("images", "源文件不存在" + path);
                }

                File dir = new File(IMAGE_DIR);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                final File file = new File(dir + "/temp_photo" + System.currentTimeMillis() + ".jpg");
                saveBitmapToFile(path,file.getAbsolutePath());
                if (file.exists()) {
                    Log.d("images", "压缩后的文件存在" + file.getAbsolutePath());
                } else {
                    Log.d("images", "压缩后的不存在" + file.getAbsolutePath());
                }
                Message message = new Message();
                message.what = 0xAAAAAAAA;
                message.obj = file.getAbsolutePath();
                handler.sendMessage(message);

            }
        }.start();

    }
    public String saveBitmapToFile(String file, String newpath) {
        try {
            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            File aa = new File(newpath);

            FileOutputStream outputStream = new FileOutputStream(aa);

            //choose another format if PNG doesn't suit you

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);


            String filepath = aa.getAbsolutePath();
            imageUrls.add(filepath);

            return filepath;
        } catch (Exception e) {
            return null;
        }
    }
    public void photoPath(String path) {
        Map<String,Object> map=new HashMap<>();
        map.put("path",path);
        datas.add(map);
        Log.d("我是照片=",""+map);
        gridViewAddImgesAdpter.notifyDataSetChanged();
    }

    private void doUpload() {
        UrlUtil baseUrlUtil = new UrlUtil();
        baseUrlUtil.setType("/image");
        baseUrlUtil.setRoute("/upload");
        String url = baseUrlUtil.toString();

        MediaType MEDIA_TYPE_PNG = MediaType.parse("multipart/form-data");

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for(String imageUrl : imageUrls) {
            File f = new File(imageUrl);
            builder.addFormDataPart("file", f.getName(), RequestBody.create(MEDIA_TYPE_PNG, f));
        }

        final MultipartBody requestBody = builder.build();

        NetworkClient.postRequest(url, requestBody, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PublishActivity.this, "网络请求错误，请重试～", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                String responseJsonData = response.body().string();
                // 解析json
                hasResult = parseImgJSON(responseJsonData);
                Log.d("okhttp", "code: " + code);
                Log.d("okhttp", "body: " + responseJsonData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(hasResult){
                            doUploadData();
                        }
                        //  失败
                        else{
                            Toast.makeText(PublishActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        });
    }

    private void doUploadData() {
        HouseInfo houseInfo = new HouseInfo();
        houseInfo.setTitle(title);
        houseInfo.setType(houseType);
        houseInfo.setPrice(price);
        houseInfo.setCity(city);
        houseInfo.setRegionInfo(region);
        houseInfo.setAreaInfo(area);
        houseInfo.setPositionInfo(position);
        houseInfo.setDetail("https://bj.lianjia.com/");
        houseInfo.setImg(newImageUrls.get(0));
        houseInfo.setUid(uid);

        UrlUtil baseUrlUtil = new UrlUtil();
        baseUrlUtil.setType(type);
        baseUrlUtil.setRoute(route);
        String url = baseUrlUtil.toString();

        Gson gson = new Gson();
        //使用Gson将对象转换为json字符串
        String json = gson.toJson(houseInfo);

        //MediaType  设置Content-Type 标头中包含的媒体类型值
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);

        NetworkClient.postRequest(url, requestBody, new okhttp3.Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();

                PublishActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PublishActivity.this, "网络请求错误，请重试～", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call,@NotNull Response response) throws IOException {
                String responseJsonData = response.body().string();
                // 解析json
                hasResult = parseJSON(responseJsonData);
                PublishActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(hasResult){
                            Toast.makeText(PublishActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                        //  失败
                        else{
                            Toast.makeText(PublishActivity.this, "发布失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.pickCity :
                showPickerViewCity();
                break;
            case R.id.btnOk :
                title = etTitle.getText().toString();
                area = etArea.getText().toString();
                position = etPosition.getText().toString();
                des = etDes.getText().toString();
                if(etPrice.getText().toString()==null || title=="" || area=="" || position=="" || des==""
                || city=="" || region=="") {
                    Toast.makeText(PublishActivity.this, "请输入完所有内容", Toast.LENGTH_SHORT).show();
                }else {
                    price = Integer.valueOf(etPrice.getText().toString());
                    doUpload();
                }
                break;
            case R.id.btnCancel :
                onBackPressed();
                break;
        }
    }

}
