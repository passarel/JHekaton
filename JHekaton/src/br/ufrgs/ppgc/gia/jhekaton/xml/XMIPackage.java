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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java de anonymous complex type.
 * 
 * <p>O seguinte fragmento do esquema especifica o conteúdo esperado contido dentro desta classe.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://schema.omg.org/spec/XMI/2.1}Difference"/>
 *         &lt;element ref="{http://schema.omg.org/spec/XMI/2.1}Add"/>
 *         &lt;element ref="{http://schema.omg.org/spec/XMI/2.1}Replace"/>
 *         &lt;element ref="{http://schema.omg.org/spec/XMI/2.1}Delete"/>
 *         &lt;element ref="{http://schema.omg.org/spec/XMI/2.1}XMI"/>
 *         &lt;element ref="{http://schema.omg.org/spec/XMI/2.1}Documentation"/>
 *         &lt;element ref="{http://schema.omg.org/spec/XMI/2.1}Extension"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "differenceOrAddOrReplace"
})
@XmlRootElement(name = "XMIPackage")
public class XMIPackage {

    @XmlElements({
        @XmlElement(name = "Difference", namespace = "http://schema.omg.org/spec/XMI/2.1", type = Difference.class),
        @XmlElement(name = "Add", namespace = "http://schema.omg.org/spec/XMI/2.1", type = Add.class),
        @XmlElement(name = "Replace", namespace = "http://schema.omg.org/spec/XMI/2.1", type = Replace.class),
        @XmlElement(name = "Delete", namespace = "http://schema.omg.org/spec/XMI/2.1", type = Delete.class),
        @XmlElement(name = "XMI", namespace = "http://schema.omg.org/spec/XMI/2.1", type = XMI.class),
        @XmlElement(name = "Documentation", namespace = "http://schema.omg.org/spec/XMI/2.1", type = Documentation.class),
        @XmlElement(name = "Extension", namespace = "http://schema.omg.org/spec/XMI/2.1", type = Extension.class)
    })
    protected List<Object> differenceOrAddOrReplace;

    /**
     * Gets the value of the differenceOrAddOrReplace property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the differenceOrAddOrReplace property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDifferenceOrAddOrReplace().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Difference }
     * {@link Add }
     * {@link Replace }
     * {@link Delete }
     * {@link XMI }
     * {@link Documentation }
     * {@link Extension }
     * 
     * 
     */
    public List<Object> getDifferenceOrAddOrReplace() {
        if (differenceOrAddOrReplace == null) {
            differenceOrAddOrReplace = new ArrayList<Object>();
        }
        return this.differenceOrAddOrReplace;
    }

}
