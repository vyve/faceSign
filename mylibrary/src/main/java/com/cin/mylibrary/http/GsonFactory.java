package com.cin.mylibrary.http;

import com.blankj.utilcode.util.LogUtils;
import com.cin.mylibrary.base.BaseModel;
import com.cin.mylibrary.bean.BaseResponseBean;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Converter;
import retrofit2.Retrofit;

import static okhttp3.internal.Util.UTF_8;

/**
 * 自定义Convert对网络请求结果的统一处理
 * Created by 王新超 on 2018/1/31.
 */

public class GsonFactory extends Converter.Factory {

    private final Gson gson;

    private GsonFactory(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        this.gson = gson;
    }

    public static GsonFactory create() {
        return create(new Gson());
    }

    public static GsonFactory create(Gson gson) {
        return new GsonFactory(gson);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new CustomGsonResponseBodyConverter<>(gson, adapter);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new CustomGsonRequestBodyConverter<>(gson, adapter);
    }

    static class CustomGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        private final Gson gson;
        private final TypeAdapter<T> adapter;

        CustomGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
            this.gson = gson;
            this.adapter = adapter;
        }

        @Override
        public T convert(ResponseBody value) throws IOException {
            String response = value.string();
            LogUtils.i("请求结果", response);
            /*
             * Token 失效或者缺失 需要connect 重新获取Token，然后重复请求刚才那个接口
             */
            BaseResponseBean bean = gson.fromJson(response, BaseResponseBean.class);
            int code = bean.getCode();
            if (code!=200){
                value.close();
                throw new ApiException(code, bean.getMessage());
            }
//            /*
//             * Token缺失
//             */
//            if (code.equals("0003") && httpStatus.getHeader().getRstInfo().equals("token为空")) {
//                value.close();
//                throw new TokenNotExistException();
//                /*
//                 * Token失效
//                 */
//            } else if (code.equals("0001")) {
//                value.close();
//                throw new TokenInvalidException();
//                /*
//                 * 其他错误
//                 */
//            } else if (!httpStatus.getHeader().getRstCode().equals("0000")) {
//                value.close();
//                throw new ApiException(httpStatus.getHeader().getRstCode(), httpStatus.getHeader().getRstInfo());
//            }
            /*
             * 数据正常，继续向下传递
             */
            MediaType contentType = value.contentType();
            Charset charset = contentType != null ? contentType.charset(UTF_8) : UTF_8;
            InputStream inputStream = new ByteArrayInputStream(response.getBytes());
            Reader reader = new InputStreamReader(inputStream, charset);
            JsonReader jsonReader = gson.newJsonReader(reader);

            try {
                return adapter.read(jsonReader);
            } finally {
                value.close();
            }
        }
    }

    static class CustomGsonRequestBodyConverter<T> implements Converter<T, RequestBody> {
        private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
        private static final Charset UTF_8 = StandardCharsets.UTF_8;

        private final Gson gson;
        private final TypeAdapter<T> adapter;

        CustomGsonRequestBodyConverter(Gson gson, TypeAdapter<T> adapter) {
            this.gson = gson;
            this.adapter = adapter;
        }

        @Override
        public RequestBody convert(T value) throws IOException {
            Buffer buffer = new Buffer();
            Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
            JsonWriter jsonWriter = gson.newJsonWriter(writer);
            adapter.write(jsonWriter, value);
            jsonWriter.close();
            return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
        }
    }

}
