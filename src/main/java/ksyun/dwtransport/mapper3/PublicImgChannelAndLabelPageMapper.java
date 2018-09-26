package ksyun.dwtransport.mapper3;

import org.apache.ibatis.annotations.Insert;
import ksyun.dwtransport.pojo.PublicImgChannelAndLabelPage;

public interface PublicImgChannelAndLabelPageMapper {

	@Insert("insert into public_img_channelandlabelpage (label_id,labelchannelpage,urllist) values (#{label_id},#{labelchannelpage},#{urllist})")
	void insert(PublicImgChannelAndLabelPage publicImgChannelAndLabelPage);
}
