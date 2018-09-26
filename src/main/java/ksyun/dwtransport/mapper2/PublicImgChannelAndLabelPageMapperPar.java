package ksyun.dwtransport.mapper2;

import org.apache.ibatis.annotations.Insert;
import ksyun.dwtransport.pojo.PublicImgChannelAndLabelPage;

public interface PublicImgChannelAndLabelPageMapperPar {

	@Insert("insert into public_img_channelandlabelpage (label_id,labelchannelpage,urllist) values (#{label_id},#{labelchannelpage},#{urllist})")
	void insert(PublicImgChannelAndLabelPage publicImgChannelAndLabelPage);
}
