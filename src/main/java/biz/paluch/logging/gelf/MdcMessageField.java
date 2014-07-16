package biz.paluch.logging.gelf;

/**
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MdcMessageField)) {
            return false;
        }

        MdcMessageField that = (MdcMessageField) o;

        if (mdcName != null ? !mdcName.equals(that.mdcName) : that.mdcName != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (mdcName != null ? mdcName.hashCode() : 0);
        return result;
    }
}
