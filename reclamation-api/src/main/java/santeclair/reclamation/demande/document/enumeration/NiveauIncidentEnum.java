package santeclair.reclamation.demande.document.enumeration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import santeclair.lunar.framework.enumeration.AbstractEnumTools;
import santeclair.lunar.framework.enumeration.CodeEnum;
import santeclair.lunar.framework.enumeration.LibelleEnum;

@XmlEnum
public enum NiveauIncidentEnum implements
                CodeEnum<NiveauIncidentEnum>,
                LibelleEnum<NiveauIncidentEnum> {

    @XmlEnumValue("0")
    ZERO("0", "0"),
    @XmlEnumValue("1")
    UN("1", "1"),
    @XmlEnumValue("2")
    DEUX("2", "2"),
    @XmlEnumValue("3")
    TROIS("3", "3"),
    @XmlEnumValue("4")
    QUATRE("4", "4"),
    @XmlEnumValue("5")
    CINQ("5", "5");

    /**
     * Le code du niveau d'incident
     */
    private String code;

    /**
     * Le libell� du niveau d'incident
     */
    private String libelle;

    /* ======================================================= *
     *                      constructeurs 
     * ======================================================= */

    private NiveauIncidentEnum(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

    /* ======================================================= *
     *                      getters & setters 
     * ======================================================= */

    /**
     * @return the code
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * @return the libelle
     */
    @Override
    public String getLibelle() {
        return libelle;
    }

    /**
     * Retourne le NiveauIncidentEnum correspondant au code pass� en param�tre
     * 
     * @param code
     * @return NiveauIncidentEnum
     */
    public static NiveauIncidentEnum byCode(String code) {
        return AbstractEnumTools.findEnumValuesByCode(
                        NiveauIncidentEnum.class, code);
    }
}
