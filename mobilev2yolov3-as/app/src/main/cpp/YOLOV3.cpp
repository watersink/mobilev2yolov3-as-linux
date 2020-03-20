//
// Created by root on 6/3/19.
//

#include "YOLOV3.h"
#include <android/log.h>
#define TAG "yolov3"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)

YOLOV3::YOLOV3(string param_path, string bin_path) {

    const char *param_path_char = param_path.c_str();
    const char *bin_path_char = bin_path.c_str();


    int ret_param = yolov3_net.load_param_bin(param_path_char);
    int ret_bin = yolov3_net.load_model(bin_path_char);

	std::cout<<"### "<<ret_param<<" "<<ret_bin<<std::endl;


}


YOLOV3::YOLOV3(ncnn::Mat param_path, ncnn::Mat bin_path) {


    int ret_param = yolov3_net.load_param((const unsigned char *)param_path);
    int ret_bin = yolov3_net.load_model((const unsigned char *)bin_path);

    LOGD("############### %d  %d", ret_param,ret_bin);
    //std::cout<<"### "<<ret_param<<" "<<ret_bin<<std::endl;

}


ncnn::Mat YOLOV3::detect(ncnn::Mat in) {

    int img_w = in.w;
    int img_h = in.h;



    in.substract_mean_normalize(mean_vals, norm_vals);

    ncnn::Extractor ex = yolov3_net.create_extractor();
    ex.set_num_threads(2);

    ex.input(mobilenetv2_yolov3_param_id::BLOB_data, in);

    ncnn::Mat out;
    ex.extract(mobilenetv2_yolov3_param_id::BLOB_detection_out, out);

    return out;
}





YOLOV3::~YOLOV3() {

}
