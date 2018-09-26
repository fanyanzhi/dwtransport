package ksyun.dwtransport.mapper;

import org.apache.ibatis.annotations.Insert;

import ksyun.dwtransport.pojo.PublicImgChannelAndLabelTask;

public interface PublicImgChannelAndLabelTaskMapper {

	@Insert("insert into public_img_channelandlabeltask(time, type) values(#{time},#{type})")
	void insert(PublicImgChannelAndLabelTask publicImgChannelAndLabelTask);
}
