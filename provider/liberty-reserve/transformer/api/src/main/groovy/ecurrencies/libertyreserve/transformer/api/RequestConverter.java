package ecurrencies.libertyreserve.transformer.api;

import com.google.protobuf.Message;

public interface RequestConverter<Request extends Message> {

    String convert(Request message);
}
