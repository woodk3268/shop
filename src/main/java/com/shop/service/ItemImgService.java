package com.shop.service;

import com.shop.dto.ItemImgDto;
import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemImgService {

    @Value("${itemImgLocation}")
    private String itemImgLocation;

    private final ItemImgRepository itemImgRepository;

    private final FileService fileService;

    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception{

        //저장할 경로, 원본명, 파일 데이터 넘겨서 file upload
        //imgUrl = /images/item/ 지정
        String oriImgName = itemImgFile.getOriginalFilename();
        String imgName="";
        String imgUrl="";

        if(!StringUtils.isEmpty(oriImgName)){
            imgName=fileService.uploadFile(itemImgLocation, oriImgName,
                   itemImgFile.getBytes() );
            imgUrl = "/images/item/" + imgName;
        }
        //itemimg 처음에 넘어올 때, id , rep 지정된채로 넘어옴
        //원본명, 저장된 이름, img url 업데이트
        //repository에 저장
        itemImg.updateItemImg(oriImgName, imgName, imgUrl);
        itemImgRepository.save(itemImg);
    }
    public List<ItemImgDto> getItemImgDtoList(Long itemId){
        //itemImgRepository에서 itemid로 찾은 다음
        //엔티티를 dto로 변환
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        List<ItemImgDto> itemImgDtoList = new ArrayList<>();

        for(ItemImg itemImg : itemImgList){
            ItemImgDto itemImgDto = ItemImgDto.of(itemImg);
            itemImgDtoList.add(itemImgDto);
        }
        return itemImgDtoList;
    }
    public void updateItemImg(Long itemImgId, MultipartFile itemImgFile) throws Exception{
        //수정한 파일 있으면 itemImgRepository 에서 itemImgId로 찾아옴
        if(!itemImgFile.isEmpty()){
            ItemImg savedItemImg = itemImgRepository.findById(itemImgId)
                    .orElseThrow(EntityNotFoundException::new);
            //찾아온 파일이 있으면 파일 삭제
            if(!StringUtils.isEmpty(savedItemImg.getImgName())){
                fileService.deleteFile(itemImgLocation+"/"+savedItemImg.getImgName());
            }
            //원본명, 저장이름, url 넘겨서 updateitemimg
            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
            String imgUrl = "/images/item/" + imgName;
            savedItemImg.updateItemImg(oriImgName, imgName, imgUrl);
        }
    }


}
