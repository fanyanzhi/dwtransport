package ksyun.dwtransport.pojo;


import lombok.Data;

import java.util.Date;

@Data
/**
 *
 *
 * @author HANPEILI
 *
 */
public class PublicImgChannels {
    private Long id;
    private String md5;
    private Integer origin_source_id;
    private Long channel_id;
    private String ks3_url;
    private Date date_time;
}
