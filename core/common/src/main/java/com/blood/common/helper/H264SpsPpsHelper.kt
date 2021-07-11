package com.blood.common.helper

class H264SpsPpsHelper(startBit: Int = 32) {

    private val h264Parser = H264Parser(startBit)
    private var forbidden_zero_bit: Int = 0
    private var nal_ref_idc: Int = 0
    private var nal_unit_type: Int = 0
    private var profile_idc: Int = 0
    private var constraint_set0_flag: Int = 0
    private var constraint_set1_flag: Int = 0
    private var constraint_set2_flag: Int = 0
    private var constraint_set3_flag: Int = 0
    private var reserved_zero_4bits: Int = 0
    private var level_idc: Int = 0
    private var seq_parameter_set_id: Int = 0
    private var chroma_format_idc: Int = 0
    private var residual_colour_transform_flag: Int = 0
    private var bit_depth_luma_minus8: Int = 0
    private var bit_depth_chroma_minus8: Int = 0
    private var qpprime_y_zero_transform_bypass_flag: Int = 0
    private var seq_scaling_matrix_present_flag: Int = 0
    private var seq_scaling_list_present_flag = IntArray(0)
    private var log2_max_frame_num_minus4: Int = 0
    private var pic_order_cnt_type: Int = 0
    private var log2_max_pic_order_cnt_lsb_minus4: Int = 0
    private var delta_pic_order_always_zero_flag: Int = 0
    private var offset_for_non_ref_pic: Int = 0
    private var offset_for_top_to_bottom_field: Int = 0
    private var num_ref_frames_in_pic_order_cnt_cycle: Int = 0
    private var offset_for_ref_frame: IntArray = IntArray(0)
    private var num_ref_frames: Int = 0
    private var gaps_in_frame_num_ue_allowed_flag: Int = 0
    private var pic_width_in_mbs_minus1: Int = 0
    private var pic_height_in_map_units_minus1: Int = 0

    fun parse(bytes: ByteArray): Boolean {
        forbidden_zero_bit = h264Parser.u(bytes, 1)
        nal_ref_idc = h264Parser.u(bytes, 2)
        nal_unit_type = h264Parser.u(bytes, 5)
        if (nal_unit_type == 7) {
            profile_idc = h264Parser.u(bytes, 8)
            constraint_set0_flag = h264Parser.u(bytes, 1) //(bytes[1] & 0x80)>>7;
            constraint_set1_flag = h264Parser.u(bytes, 1) //(bytes[1] & 0x40)>>6;
            constraint_set2_flag = h264Parser.u(bytes, 1) //(bytes[1] & 0x20)>>5;
            constraint_set3_flag = h264Parser.u(bytes, 1) //(bytes[1] & 0x10)>>4;
            reserved_zero_4bits = h264Parser.u(bytes, 4)
            level_idc = h264Parser.u(bytes, 8)
            seq_parameter_set_id = h264Parser.ue(bytes)
            if (profile_idc == 100 || profile_idc == 110 || profile_idc == 122 || profile_idc == 144) {
                chroma_format_idc = h264Parser.ue(bytes)
                if (chroma_format_idc == 3) {
                    residual_colour_transform_flag = h264Parser.u(bytes, 1)
                }
                bit_depth_luma_minus8 = h264Parser.ue(bytes)
                bit_depth_chroma_minus8 = h264Parser.ue(bytes)
                qpprime_y_zero_transform_bypass_flag = h264Parser.u(bytes, 1)
                seq_scaling_matrix_present_flag = h264Parser.u(bytes, 1)
                seq_scaling_list_present_flag = IntArray(8)
                if (seq_scaling_matrix_present_flag != 0) {
                    for (i in 0..7) {
                        seq_scaling_list_present_flag[i] = h264Parser.u(bytes, 1)
                    }
                }
            }
            log2_max_frame_num_minus4 = h264Parser.ue(bytes)
            pic_order_cnt_type = h264Parser.ue(bytes)
            if (pic_order_cnt_type == 0) {
                log2_max_pic_order_cnt_lsb_minus4 = h264Parser.ue(bytes)
            } else if (pic_order_cnt_type == 1) {
                delta_pic_order_always_zero_flag = h264Parser.u(bytes, 1)
                offset_for_non_ref_pic = h264Parser.se(bytes)
                offset_for_top_to_bottom_field = h264Parser.se(bytes)
                num_ref_frames_in_pic_order_cnt_cycle = h264Parser.ue(bytes)
                offset_for_ref_frame = IntArray(num_ref_frames_in_pic_order_cnt_cycle)
                for (i in 0 until num_ref_frames_in_pic_order_cnt_cycle) {
                    offset_for_ref_frame[i] = h264Parser.se(bytes)
                }
            }
            num_ref_frames = h264Parser.ue(bytes)
            gaps_in_frame_num_ue_allowed_flag = h264Parser.u(bytes, 1)
            pic_width_in_mbs_minus1 = h264Parser.ue(bytes)
            pic_height_in_map_units_minus1 = h264Parser.ue(bytes)
            return true
        }
        return false
    }

    // 与真实屏幕可能不符，因为宽高必须是最大宏块的个数，即16的倍数
    fun getSize(): IntArray {
        val size = IntArray(2)
        // 必须加一，编码规范有关
        size[0] = (pic_width_in_mbs_minus1 + 1) * 16
        size[1] = (pic_height_in_map_units_minus1 + 1) * 16
        return size
    }

    override fun toString(): String {
        return "H264SpsPpsHelper(forbidden_zero_bit=$forbidden_zero_bit, nal_ref_idc=$nal_ref_idc, nal_unit_type=$nal_unit_type, profile_idc=$profile_idc, constraint_set0_flag=$constraint_set0_flag, constraint_set1_flag=$constraint_set1_flag, constraint_set2_flag=$constraint_set2_flag, constraint_set3_flag=$constraint_set3_flag, reserved_zero_4bits=$reserved_zero_4bits, level_idc=$level_idc, seq_parameter_set_id=$seq_parameter_set_id, chroma_format_idc=$chroma_format_idc, residual_colour_transform_flag=$residual_colour_transform_flag, bit_depth_luma_minus8=$bit_depth_luma_minus8, bit_depth_chroma_minus8=$bit_depth_chroma_minus8, qpprime_y_zero_transform_bypass_flag=$qpprime_y_zero_transform_bypass_flag, seq_scaling_matrix_present_flag=$seq_scaling_matrix_present_flag, seq_scaling_list_present_flag=${seq_scaling_list_present_flag.contentToString()}, log2_max_frame_num_minus4=$log2_max_frame_num_minus4, pic_order_cnt_type=$pic_order_cnt_type, log2_max_pic_order_cnt_lsb_minus4=$log2_max_pic_order_cnt_lsb_minus4, delta_pic_order_always_zero_flag=$delta_pic_order_always_zero_flag, offset_for_non_ref_pic=$offset_for_non_ref_pic, offset_for_top_to_bottom_field=$offset_for_top_to_bottom_field, num_ref_frames_in_pic_order_cnt_cycle=$num_ref_frames_in_pic_order_cnt_cycle, offset_for_ref_frame=${offset_for_ref_frame.contentToString()}, num_ref_frames=$num_ref_frames, gaps_in_frame_num_ue_allowed_flag=$gaps_in_frame_num_ue_allowed_flag, pic_width_in_mbs_minus1=$pic_width_in_mbs_minus1, pic_height_in_map_units_minus1=$pic_height_in_map_units_minus1)"
    }

}