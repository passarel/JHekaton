//
// Este arquivo foi gerado pela Arquitetura JavaTM para Implementação de Referência (JAXB) de Bind XML, v2.2.6 
// Consulte <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas as modificações neste arquivo serão perdidas após a recompilação do esquema de origem. 
// Gerado em: PM.03.19 às 07:12:05 PM BRT 
//


package br.ufrgs.ppgc.gia.jhekaton.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java de Replace complex type.
 * 
 * <p>O seguinte fragmento do esquema especifica o conteúdo esperado contido dentro desta classe.
 * 
 * <pre>
 * &lt;complexType name="Replace">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schema.omg.org/spec/XMI/2.1}Difference">
 *       &lt;attribute name="position" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="replacement" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Replace")
public class Replace
    extends Difference
{

    @XmlAttribute(name = "position")
    protected String position;
    @XmlAttribute(name = "replacement")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> replacement;

    /**
     * Obtém o valor da propriedade position.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPosition() {
        return position;
    }

    /**
     * Define o valor da propriedade position.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPosition(String value) {
        this.position = value;
    }

    /**
     * Gets the value of the replacement property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the replacement property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReplacement().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getReplacement() {
        if (replacement == null) {
            replacement = new ArrayList<Object>();
        }
        return this.replacement;
    }

}
