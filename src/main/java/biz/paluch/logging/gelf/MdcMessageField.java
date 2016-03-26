package biz.paluch.logging.gelf;

/**
 * @author Mark Paluch
 */
public class MdcMessageField implements MessageField {

    private String name;
    private String mdcName;

    public MdcMessageField(String name, String mdcName) {
        this.mdcName = mdcName;
        this.name = name;
    }

    public String getMdcName() {
        return mdcName;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(getClass().getSimpleName());
        sb.append(" [name='").append(name).append('\'');
        sb.append(", mdcName='").append(mdcName).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
