package com.blood.h264.parse.columbus;

import com.blood.common.helper.H264SpsPpsHelper;
import com.blood.common.util.StringUtil;

import java.util.Arrays;

public class H264SpsPpsTest {

    public static void main(String[] args) {
//        String hex = "0000000167640032ACB40220087F2CD2905060606D0A13500000000168EE06F2C0";
        String hex = "0000000167640028ACCA7014016EC05A808080A0000003002000000F01E30614F00000000168EBEF2C";
        byte[] bytes = StringUtil.hexStringToByteArray(hex);
        H264SpsPpsHelper spsPpsHelper = new H264SpsPpsHelper();
        spsPpsHelper.parse(bytes);
        int[] size = spsPpsHelper.getSize();
        System.out.println(spsPpsHelper);
        System.out.println(Arrays.toString(size));
    }

/*
1080 2150

最大宏块 16 的倍数

0000000167640032ACB40220087F2CD2905060606D0A13500000000168EE06F2C0
H264SpsPpsHelper(forbidden_zero_bit=0, nal_ref_idc=3, nal_unit_type=7, profile_idc=100, constraint_set0_flag=0, constraint_set1_flag=0, constraint_set2_flag=0, constraint_set3_flag=0, reserved_zero_4bits=0, level_idc=50, seq_parameter_set_id=0, chroma_format_idc=1, residual_colour_transform_flag=0, bit_depth_luma_minus8=0, bit_depth_chroma_minus8=0, qpprime_y_zero_transform_bypass_flag=0, seq_scaling_matrix_present_flag=0, seq_scaling_list_present_flag=[0, 0, 0, 0, 0, 0, 0, 0], log2_max_frame_num_minus4=0, pic_order_cnt_type=2, log2_max_pic_order_cnt_lsb_minus4=0, delta_pic_order_always_zero_flag=0, offset_for_non_ref_pic=0, offset_for_top_to_bottom_field=0, num_ref_frames_in_pic_order_cnt_cycle=0, offset_for_ref_frame=[], num_ref_frames=1, gaps_in_frame_num_ue_allowed_flag=0, pic_width_in_mbs_minus1=67, pic_height_in_map_units_minus1=134)
[1088, 2160]

0000000167640028ACCA7014016EC05A808080A0000003002000000F01E30614F00000000168EBEF2C
H264SpsPpsHelper(forbidden_zero_bit=0, nal_ref_idc=3, nal_unit_type=7, profile_idc=100, constraint_set0_flag=0, constraint_set1_flag=0, constraint_set2_flag=0, constraint_set3_flag=0, reserved_zero_4bits=0, level_idc=40, seq_parameter_set_id=0, chroma_format_idc=1, residual_colour_transform_flag=0, bit_depth_luma_minus8=0, bit_depth_chroma_minus8=0, qpprime_y_zero_transform_bypass_flag=0, seq_scaling_matrix_present_flag=0, seq_scaling_list_present_flag=[0, 0, 0, 0, 0, 0, 0, 0], log2_max_frame_num_minus4=0, pic_order_cnt_type=0, log2_max_pic_order_cnt_lsb_minus4=4, delta_pic_order_always_zero_flag=0, offset_for_non_ref_pic=0, offset_for_top_to_bottom_field=0, num_ref_frames_in_pic_order_cnt_cycle=0, offset_for_ref_frame=[], num_ref_frames=6, gaps_in_frame_num_ue_allowed_flag=0, pic_width_in_mbs_minus1=79, pic_height_in_map_units_minus1=44)
[1280, 720]

*/

}
