package ksyun.dwtransport.mapper2;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PublicImgLabelsMapper {

	@Select("SELECT distinct label_id FROM public_img_labels where md5 = #{md5}")
	List<Integer> getLabelIdsByMd5(@Param("md5")String md5); 
}
