package haivo.chatbot.dto;

public record BillItem(String itemName,
                       Integer quantity,
                       double price,
                       double subTotal) {

}
