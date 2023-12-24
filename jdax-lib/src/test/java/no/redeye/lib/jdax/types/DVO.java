package no.redeye.lib.jdax.types;



/**
 *
 */
public record DVO(String id, String number, String name) implements VO {

    public DVO(String number, String name) {
        this("-1", number, name);
    }
}
