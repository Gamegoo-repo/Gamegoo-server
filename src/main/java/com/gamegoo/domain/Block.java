package com.gamegoo.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "Block")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "block_id")
    private Long id;
}
