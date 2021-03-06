package santeclair.reclamation.demande.document.enumeration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import santeclair.lunar.framework.enumeration.AbstractEnumTools;
import santeclair.lunar.framework.enumeration.CodeEnum;
import santeclair.lunar.framework.enumeration.LibelleEnum;

@XmlEnum
public enum EtatDemandeEnum implements
                CodeEnum<EtatDemandeEnum>,
                LibelleEnum<EtatDemandeEnum> {

    @XmlEnumValue("ATTENTE")
    ATTENTE_RECEPTION("ATTENTE", "En attente de r�ception"),
    @XmlEnumValue("PARTIELLE")
    RECEPTION_PARTIELLE("PARTIELLE", "R�ception partielle"),
    @XmlEnumValue("COMPLETE")
    RECEPTION_COMPLETE("COMPLETE", "R�ception compl�te"),
    @XmlEnumValue("ANALYSE")
    DOSSIER_ANALYSE("ANALYSE", "Dossier analys�");

    /**
     * Le code de l'�tat de la demande de document
     */
    private final String code;

    /**
     * Le libell� de l'�tat de la demande de document
     */
    private final String libelle;

    /* ======================================================= *
     *                      constructeur
     * ======================================================= */

    private EtatDemandeEnum(final String code, final String libelle) {
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
     * Retourne l'EtatDemandeEnum correspondant au code pass� en param�tre
     * 
     * @param code
     * @return EtatDemandeEnum
     */
    public static EtatDemandeEnum byCode(String code) {
        return AbstractEnumTools.findEnumValuesByCode(
                        EtatDemandeEnum.class, code);
    }
    
    @Override
    public String toString() {
        return libelle;
    }
}
