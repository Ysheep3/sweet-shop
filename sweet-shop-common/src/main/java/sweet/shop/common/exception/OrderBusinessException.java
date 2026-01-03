package sweet.shop.common.exception;

public class OrderBusinessException extends BaseException {
    public OrderBusinessException(String message) {
        super(message);
    }

    public OrderBusinessException() {
        super();
    }
}
