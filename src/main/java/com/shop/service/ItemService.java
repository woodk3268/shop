package com.shop.service;


import com.shop.dto.ItemFormDto;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.repository.item.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemImgService itemImgService;


    public Long saveItem(ItemFormDto itemFormDto,
                         List<MultipartFile> itemImgFileList) throws Exception{
        //Item dto를 엔티티로 변환
        //repository에 저장
        Item item = itemFormDto.createItem();
        itemRepository.save(item);

        //imgfilelist 사이즈만큼 반복
        //item img 생성, item 연관관계 설정, 대표이미지 여부
        //itemimgservice호출. itemimg 엔티티, 파일 데이터 넘김.
        for(int i=0;i<itemImgFileList.size();i++){
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);
            if(i==0) itemImg.setRepimgYn("Y");
            else itemImg.setRepimgYn("N");
            itemImgService.saveItemImg(itemImg, itemImgFileList.get(i));
        }
        //item id 반환
        return item.getId();
    }
    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId){
        //itemid 로  repository에서 찾아오기
        //엔티티를 dto로 변환
        //itemimgdto list set
        //dto 반환
        Item item = itemRepository.findById(itemId)
        .orElseThrow(EntityNotFoundException::new);
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgService.getItemImgDtoList(itemId));
        return itemFormDto;
    }
    public Long updateItem(ItemFormDto itemFormDto,
                           List<MultipartFile> itemImgFileList) throws Exception{
        //itemid 로 repository에서 찾아옴.
        //dto의 필드들로 item 엔티티 업데이트
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(EntityNotFoundException::new);
        item.updateItem(itemFormDto);

        //dto의 imgids
        List<Long> itemImgIds = itemFormDto.getItemImgIds();

        //itemimgfilelist size 만큼 반복
        //하나씩다 update
        for(int i=0;i<itemImgFileList.size();i++){
            itemImgService.updateItemImg(itemImgIds.get(i),
                    itemImgFileList.get(i));
        }
        return item.getId();
    }
    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }
    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto,
                                             Pageable pageable){
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }


}
