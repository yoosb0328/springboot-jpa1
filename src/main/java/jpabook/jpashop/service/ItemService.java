package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item){
        itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity) {
        //변경감지 업데이트 방식
        Item findItem = itemRepository.findOne(itemId); //find로 찾아와서 영속성 컨텍스트에 등록한다.
        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity); //영속 상태이므로 변경감지 가능.
        /*
        실제로는 setter가 아니라 변경을 위한 의미있는 편의메서드를 엔티티에 구현하여 사용하는 것이 추적에 용이하다.
         */
    }

    public List<Item> findAll(){
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId){
        return itemRepository.findOne(itemId);
    }
}
