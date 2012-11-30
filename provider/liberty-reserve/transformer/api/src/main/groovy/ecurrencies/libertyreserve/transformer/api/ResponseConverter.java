package ecurrencies.libertyreserve.transformer.api;

import com.google.protobuf.Message;

public interface ResponseConverter<Response extends Message> {

    Response convert(String response);

}
