package ksyun.dwtransport.mapper;

import org.apache.ibatis.annotations.Insert;

import ksyun.dwtransport.pojo.PublicImgChannelAndLabel;

public interface PublicImgChannelAndLabelMapper {

	@Insert("insert into public_img_channelandlabel (md5,origin_source_id,channel_id,label_id,ks3_url,date_time) values (#{md5},#{origin_source_id},#{channel_id},#{label_id},#{ks3_url},#{date_time})")
	void insert(PublicImgChannelAndLabel publicImgChannelAndLabel);
}
